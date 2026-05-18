package com.sentinela.camtv.ui.cameras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.data.camera.CameraRepository
import com.sentinela.camtv.data.onvif.OnvifRepository
import com.sentinela.camtv.domain.Camera
import com.sentinela.onvif.DiscoveredOnvifDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CameraManagerUiState(
    val cameras: List<Camera> = emptyList(),
    val discoveredDevices: List<DiscoveredOnvifDevice> = emptyList(),
    val scanning: Boolean = false,
    val authDialogMessage: String? = null,
    val statusMessage: String? = null,
)

class CameraManagerViewModel(
    private val cameraRepository: CameraRepository,
    private val onvifRepository: OnvifRepository,
) : ViewModel() {
    private val discoveredDevices = MutableStateFlow<List<DiscoveredOnvifDevice>>(emptyList())
    private val scanning = MutableStateFlow(false)
    private val authDialogMessage = MutableStateFlow<String?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<CameraManagerUiState> = combine(
        cameraRepository.observeAllCameras(),
        discoveredDevices,
        scanning,
        authDialogMessage,
        statusMessage,
    ) { cameras, devices, isScanning, authMessage, message ->
        CameraManagerUiState(
            cameras = cameras,
            discoveredDevices = devices,
            scanning = isScanning,
            authDialogMessage = authMessage,
            statusMessage = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CameraManagerUiState(),
    )

    fun discoverOnvifDevices() {
        viewModelScope.launch {
            scanning.value = true
            statusMessage.value = "Procurando dispositivos ONVIF..."
            onvifRepository.discover()
                .onSuccess { devices ->
                    discoveredDevices.value = devices
                    statusMessage.value = if (devices.isEmpty()) {
                        "Nenhum dispositivo ONVIF encontrado."
                    } else {
                        "${devices.size} dispositivo(s) encontrado(s)."
                    }
                }
                .onFailure { error ->
                    statusMessage.value = "Falha na descoberta ONVIF: ${error.message ?: "erro desconhecido"}"
                }
            scanning.value = false
        }
    }

    fun saveManualRtspCamera(
        name: String,
        mainRtspUrl: String,
        subRtspUrl: String?,
    ) {
        viewModelScope.launch {
            runCatching {
                cameraRepository.saveManualRtspCamera(
                    id = "rtsp-${System.currentTimeMillis()}",
                    name = name.ifBlank { "Camera RTSP" },
                    rtspUrl = mainRtspUrl,
                    subRtspUrl = subRtspUrl?.ifBlank { null },
                    position = state.value.cameras.size,
                )
            }.onSuccess {
                statusMessage.value = "Camera RTSP salva."
            }.onFailure { error ->
                authDialogMessage.value = "Não foi possível salvar a câmera: ${error.message ?: "URL inválida"}"
            }
        }
    }

    fun dismissAuthDialog() {
        authDialogMessage.value = null
    }
}
