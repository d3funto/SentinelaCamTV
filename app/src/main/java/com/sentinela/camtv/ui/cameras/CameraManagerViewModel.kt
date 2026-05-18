package com.sentinela.camtv.ui.cameras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.data.camera.CameraRepository
import com.sentinela.camtv.data.onvif.OnvifEndpointNormalizer
import com.sentinela.camtv.data.onvif.OnvifRepository
import com.sentinela.camtv.data.onvif.OnvifProfileSelector
import com.sentinela.camtv.domain.Camera
import com.sentinela.onvif.DiscoveredOnvifDevice
import com.sentinela.onvif.OnvifCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CameraManagerUiState(
    val cameras: List<Camera> = emptyList(),
    val discoveredDevices: List<DiscoveredOnvifDevice> = emptyList(),
    val selectedDeviceKey: String? = null,
    val manualOnvifAddress: String = "",
    val username: String = "",
    val password: String = "",
    val scanning: Boolean = false,
    val saving: Boolean = false,
    val authDialogMessage: String? = null,
    val statusMessage: String? = null,
) {
    val selectedDevice: DiscoveredOnvifDevice?
        get() = discoveredDevices.firstOrNull { device -> device.stableKey() == selectedDeviceKey }

    val busy: Boolean
        get() = scanning || saving
}

class CameraManagerViewModel(
    private val cameraRepository: CameraRepository,
    private val onvifRepository: OnvifRepository,
) : ViewModel() {
    private val discoveredDevices = MutableStateFlow<List<DiscoveredOnvifDevice>>(emptyList())
    private val selectedDeviceKey = MutableStateFlow<String?>(null)
    private val manualOnvifAddress = MutableStateFlow("")
    private val username = MutableStateFlow("")
    private val password = MutableStateFlow("")
    private val scanning = MutableStateFlow(false)
    private val saving = MutableStateFlow(false)
    private val authDialogMessage = MutableStateFlow<String?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<CameraManagerUiState> = combine(
        cameraRepository.observeAllCameras(),
        discoveredDevices,
        selectedDeviceKey,
        manualOnvifAddress,
        username,
        password,
        scanning,
        saving,
        authDialogMessage,
        statusMessage,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val cameras = values[0] as List<Camera>
        @Suppress("UNCHECKED_CAST")
        val devices = values[1] as List<DiscoveredOnvifDevice>
        CameraManagerUiState(
            cameras = cameras,
            discoveredDevices = devices,
            selectedDeviceKey = values[2] as String?,
            manualOnvifAddress = values[3] as String,
            username = values[4] as String,
            password = values[5] as String,
            scanning = values[6] as Boolean,
            saving = values[7] as Boolean,
            authDialogMessage = values[8] as String?,
            statusMessage = values[9] as String?,
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
                    selectedDeviceKey.value = devices.firstOrNull()?.stableKey()
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

    fun selectDiscoveredDevice(deviceKey: String) {
        selectedDeviceKey.value = deviceKey
    }

    fun updateUsername(value: String) {
        username.value = value
    }

    fun updateManualOnvifAddress(value: String) {
        manualOnvifAddress.value = value
    }

    fun updatePassword(value: String) {
        password.value = value
    }

    fun useManualOnvifAddress() {
        val normalized = OnvifEndpointNormalizer.normalize(manualOnvifAddress.value)
        if (normalized == null) {
            statusMessage.value = "Informe o IP ou URL ONVIF do dispositivo."
            return
        }
        val manualDevice = DiscoveredOnvifDevice(
            endpointReference = "manual:$normalized",
            types = listOf("dn:NetworkVideoTransmitter"),
            xAddrs = listOf(normalized),
            scopes = listOf("onvif://www.onvif.org/name/ONVIF_manual"),
        )
        discoveredDevices.value = (listOf(manualDevice) + discoveredDevices.value)
            .distinctBy { device -> device.stableKey() }
        selectedDeviceKey.value = manualDevice.stableKey()
        statusMessage.value = "Endereço ONVIF manual selecionado."
    }

    fun saveSelectedOnvifCamera() {
        viewModelScope.launch {
            val currentState = state.value
            val device = currentState.selectedDevice
            if (device == null) {
                statusMessage.value = "Selecione um dispositivo ONVIF."
                return@launch
            }

            val deviceServiceUrl = device.primaryXAddr()
            if (deviceServiceUrl.isNullOrBlank()) {
                authDialogMessage.value = "O dispositivo ONVIF não informou endereço de serviço."
                return@launch
            }

            saving.value = true
            statusMessage.value = "Consultando serviços ONVIF..."

            runCatching {
                val credentials = currentState.credentialsOrNull()
                val capabilities = onvifRepository.getCapabilities(deviceServiceUrl, credentials).getOrThrow()
                val mediaServiceUrl = capabilities.mediaXAddr ?: deviceServiceUrl

                statusMessage.value = "Consultando perfis de mídia..."
                val profiles = onvifRepository.getProfiles(mediaServiceUrl, credentials).getOrThrow()
                val selection = OnvifProfileSelector.select(profiles)
                    ?: error("Nenhum perfil de mídia ONVIF encontrado.")

                statusMessage.value = "Obtendo URL RTSP principal..."
                val mainStream = onvifRepository
                    .getStreamUri(mediaServiceUrl, selection.main.token, credentials)
                    .getOrThrow()
                val subStream = selection.sub?.let { subProfile ->
                    statusMessage.value = "Obtendo URL RTSP secundária..."
                    onvifRepository
                        .getStreamUri(mediaServiceUrl, subProfile.token, credentials)
                        .getOrThrow()
                }

                cameraRepository.saveOnvifCamera(
                    id = device.stableCameraId(),
                    name = device.displayLabel(),
                    endpoint = deviceServiceUrl,
                    onvifDeviceServiceUrl = deviceServiceUrl,
                    mainRtspUrl = mainStream.uri,
                    subRtspUrl = subStream?.uri,
                    username = currentState.username.takeIf { it.isNotBlank() },
                    password = currentState.password.takeIf { it.isNotBlank() },
                    position = currentState.cameras.size,
                )
            }.onSuccess {
                statusMessage.value = "Câmera ONVIF salva."
            }.onFailure { error ->
                val message = error.toOnvifUserMessage()
                if (error.isLikelyAuthenticationError()) {
                    authDialogMessage.value = message
                }
                statusMessage.value = message
            }

            saving.value = false
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

fun DiscoveredOnvifDevice.stableKey(): String =
    endpointReference.takeIf { it.isNotBlank() }
        ?: primaryXAddr()
        ?: scopes.joinToString("|")

fun DiscoveredOnvifDevice.displayLabel(): String =
    scopes.firstScopeValue("name")
        ?: scopes.firstScopeValue("hardware")
        ?: scopes.firstScopeValue("model")
        ?: scopes.firstScopeValue("manufacturer")
        ?: primaryXAddr()
        ?: endpointReference

private fun List<String>.firstScopeValue(scopeName: String): String? =
    firstOrNull { scope ->
        scope.contains("/$scopeName/", ignoreCase = true)
    }?.substringAfterLast('/')?.takeIf { value -> value.isNotBlank() }

private fun DiscoveredOnvifDevice.primaryXAddr(): String? =
    xAddrs.firstOrNull { address -> address.startsWith("http", ignoreCase = true) }
        ?: xAddrs.firstOrNull()

private fun DiscoveredOnvifDevice.stableCameraId(): String =
    "onvif-${Integer.toHexString(stableKey().hashCode())}"

private fun CameraManagerUiState.credentialsOrNull(): OnvifCredentials? =
    username.takeIf { it.isNotBlank() }?.let { user ->
        OnvifCredentials(
            username = user,
            password = password,
        )
    }

private fun Throwable.isLikelyAuthenticationError(): Boolean =
    message.orEmpty().lowercase().let { text ->
        "401" in text || "auth" in text || "authorized" in text || "senha" in text
    }

private fun Throwable.toOnvifUserMessage(): String =
    when {
        isLikelyAuthenticationError() ->
            "Falha de autenticação ONVIF. Confira usuário, senha e se o ONVIF está ativo no dispositivo."
        message.orEmpty().contains("timeout", ignoreCase = true) ->
            "Tempo esgotado ao consultar o dispositivo ONVIF."
        else ->
            "Falha ONVIF: ${message ?: "erro desconhecido"}"
    }
