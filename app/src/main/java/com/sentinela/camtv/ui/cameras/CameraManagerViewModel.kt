package com.sentinela.camtv.ui.cameras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.data.camera.CameraRepository
import com.sentinela.camtv.data.camera.RtspUrlSanitizer
import com.sentinela.camtv.data.onvif.OnvifRepository
import com.sentinela.camtv.data.onvif.OnvifProfileSelector
import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.player.RtspConnectionTestResult
import com.sentinela.camtv.player.RtspConnectionTester
import com.sentinela.camtv.player.userMessage
import com.sentinela.onvif.DiscoveredOnvifDevice
import com.sentinela.onvif.OnvifCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CameraManagerUiState(
    val cameras: List<Camera> = emptyList(),
    val discoveredDevices: List<DiscoveredOnvifDevice> = emptyList(),
    val selectedDeviceKey: String? = null,
    val username: String = "",
    val password: String = "",
    val rtspName: String = "",
    val rtspMainUrl: String = "",
    val rtspSubUrl: String = "",
    val rtspUsername: String = "",
    val rtspPassword: String = "",
    val scanning: Boolean = false,
    val saving: Boolean = false,
    val rtspConnecting: Boolean = false,
    val authDialogMessage: String? = null,
    val statusMessage: String? = null,
) {
    val selectedDevice: DiscoveredOnvifDevice?
        get() = discoveredDevices.firstOrNull { device -> device.stableKey() == selectedDeviceKey }

    val busy: Boolean
        get() = scanning || saving || rtspConnecting
}

class CameraManagerViewModel(
    private val cameraRepository: CameraRepository,
    private val onvifRepository: OnvifRepository,
    private val rtspConnectionTester: RtspConnectionTester,
    private val rtspCameraDraftRepository: RtspCameraDraftRepository,
) : ViewModel() {
    private val discoveredDevices = MutableStateFlow<List<DiscoveredOnvifDevice>>(emptyList())
    private val selectedDeviceKey = MutableStateFlow<String?>(null)
    private val username = MutableStateFlow("")
    private val password = MutableStateFlow("")
    private val rtspName = MutableStateFlow("")
    private val rtspMainUrl = MutableStateFlow("")
    private val rtspSubUrl = MutableStateFlow("")
    private val rtspUsername = MutableStateFlow("")
    private val rtspPassword = MutableStateFlow("")
    private val scanning = MutableStateFlow(false)
    private val saving = MutableStateFlow(false)
    private val rtspConnecting = MutableStateFlow(false)
    private val authDialogMessage = MutableStateFlow<String?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            val draft = rtspCameraDraftRepository.observeDraft().first()
            rtspName.value = draft.name
            rtspMainUrl.value = draft.mainUrl
            rtspSubUrl.value = draft.subUrl
        }
    }

    val state: StateFlow<CameraManagerUiState> = combine(
        cameraRepository.observeAllCameras(),
        discoveredDevices,
        selectedDeviceKey,
        username,
        password,
        rtspName,
        rtspMainUrl,
        rtspSubUrl,
        rtspUsername,
        rtspPassword,
        scanning,
        saving,
        rtspConnecting,
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
            username = values[3] as String,
            password = values[4] as String,
            rtspName = values[5] as String,
            rtspMainUrl = values[6] as String,
            rtspSubUrl = values[7] as String,
            rtspUsername = values[8] as String,
            rtspPassword = values[9] as String,
            scanning = values[10] as Boolean,
            saving = values[11] as Boolean,
            rtspConnecting = values[12] as Boolean,
            authDialogMessage = values[13] as String?,
            statusMessage = values[14] as String?,
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

    fun updatePassword(value: String) {
        password.value = value
    }

    fun updateRtspName(value: String) {
        rtspName.value = value
    }

    fun updateRtspMainUrl(value: String) {
        rtspMainUrl.value = value
    }

    fun updateRtspSubUrl(value: String) {
        rtspSubUrl.value = value
    }

    fun updateRtspUsername(value: String) {
        rtspUsername.value = value
    }

    fun updateRtspPassword(value: String) {
        rtspPassword.value = value
    }

    fun copyRtspMainUrlToSubUrl() {
        rtspSubUrl.value = rtspMainUrl.value
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
                statusMessage.value = "Câmera ONVIF conectada."
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

    fun connectManualRtspCamera() {
        viewModelScope.launch {
            val validation = RtspCameraFormValidator.validate(
                name = state.value.rtspName,
                mainRtspUrl = state.value.rtspMainUrl,
                subRtspUrl = state.value.rtspSubUrl,
                username = state.value.rtspUsername,
                password = state.value.rtspPassword,
            )
            if (validation is RtspCameraFormValidation.Invalid) {
                statusMessage.value = validation.message
                return@launch
            }
            val form = (validation as RtspCameraFormValidation.Valid).form
            val draft = RtspCameraDraft(
                name = form.name,
                mainUrl = form.mainRtspUrl,
                subUrl = form.subRtspUrl.orEmpty(),
            )

            rtspCameraDraftRepository.saveDraft(draft)
            rtspConnecting.value = true
            statusMessage.value = "Conectando RTSP..."

            val mainResult = testRtspUrl(
                url = form.mainRtspUrl,
                username = form.username,
                password = form.password,
                streamName = "Fluxo principal",
            )
            if (mainResult is RtspConnectionTestResult.Failure) {
                statusMessage.value = mainResult.userMessage("Fluxo principal")
                rtspConnecting.value = false
                return@launch
            }

            val subUrl = form.subRtspUrl
            if (!subUrl.isNullOrBlank()) {
                val subResult = testRtspUrl(
                    url = subUrl,
                    username = form.username,
                    password = form.password,
                    streamName = "Fluxo secundário",
                )
                if (subResult is RtspConnectionTestResult.Failure) {
                    statusMessage.value = subResult.userMessage("Fluxo secundário")
                    rtspConnecting.value = false
                    return@launch
                }
            }

            runCatching {
                cameraRepository.saveManualRtspCamera(
                    id = "rtsp-${System.currentTimeMillis()}",
                    name = form.name,
                    rtspUrl = form.mainRtspUrl,
                    subRtspUrl = form.subRtspUrl,
                    username = form.username,
                    password = form.password,
                    position = state.value.cameras.size,
                )
            }.onSuccess {
                rtspName.value = draft.name
                rtspMainUrl.value = draft.mainUrl
                rtspSubUrl.value = draft.subUrl
                rtspPassword.value = ""
                statusMessage.value = "Câmera RTSP conectada. Vá para Ver câmeras para visualizar."
            }.onFailure { error ->
                authDialogMessage.value = "Não foi possível salvar a câmera: ${error.message ?: "URL inválida"}"
            }

            rtspConnecting.value = false
        }
    }

    private suspend fun testRtspUrl(
        url: String,
        username: String?,
        password: String?,
        streamName: String,
    ): RtspConnectionTestResult {
        statusMessage.value = "Conectando $streamName..."
        return rtspConnectionTester.test(
            RtspUrlSanitizer.withCredentials(
                sanitizedUrl = url,
                username = username,
                password = password,
            ),
        )
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
