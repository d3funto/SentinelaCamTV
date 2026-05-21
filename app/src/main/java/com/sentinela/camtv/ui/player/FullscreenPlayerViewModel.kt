package com.sentinela.camtv.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.player.AudioMode
import com.sentinela.camtv.player.CameraStreamRequest
import com.sentinela.camtv.player.PlayerMode
import com.sentinela.camtv.player.TransmissionMode
import com.sentinela.camtv.player.next
import com.sentinela.camtv.player.streamRequestFor
import com.sentinela.camtv.preferences.SettingsRepository
import com.sentinela.camtv.preferences.fullscreenAudioModeFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StreamQuality {
    HD,
    SD,
}

data class FullscreenPlayerUiState(
    val camera: Camera? = null,
    val audioMode: AudioMode = AudioMode.Enabled,
    val streamQuality: StreamQuality = StreamQuality.HD,
    val showInfo: Boolean = true,
    val transmissionMode: TransmissionMode = TransmissionMode.MENOR_LATENCIA,
    val quickMenuVisible: Boolean = false,
    val showQuickMenuHint: Boolean = false,
) {
    fun streamRequest(): CameraStreamRequest? {
        val activeCamera = camera ?: return null
        val baseRequest = activeCamera.streamRequestFor(PlayerMode.Fullscreen)
        return baseRequest.copy(
            subtype = if (streamQuality == StreamQuality.HD) 0 else 1,
            audioMode = audioMode,
            transmissionMode = transmissionMode,
        )
    }
}

private data class FullscreenCoreState(
    val camera: Camera?,
    val audioMode: AudioMode,
    val streamQuality: StreamQuality,
    val showInfo: Boolean,
    val transmissionMode: TransmissionMode,
    val showQuickMenuHint: Boolean,
)

class FullscreenPlayerViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val camera = MutableStateFlow<Camera?>(null)
    private val audioOverride = MutableStateFlow<AudioMode?>(null)
    private val streamQuality = MutableStateFlow(StreamQuality.HD)
    private val transmissionModeOverride = MutableStateFlow<TransmissionMode?>(null)
    private val quickMenuVisible = MutableStateFlow(false)

    private val coreState = combine(
        camera,
        settingsRepository.observePreferences(),
        audioOverride,
        streamQuality,
        transmissionModeOverride,
    ) { activeCamera, preferences, audio, quality, localTransmissionMode ->
        FullscreenCoreState(
            camera = activeCamera,
            audioMode = audio ?: activeCamera?.let { preferences.fullscreenAudioModeFor(it) } ?: AudioMode.Enabled,
            streamQuality = quality,
            showInfo = preferences.showFullscreenInfo,
            transmissionMode = localTransmissionMode ?: preferences.globalTransmissionMode,
            showQuickMenuHint = activeCamera != null && !preferences.fullscreenQuickMenuHintSeen,
        )
    }

    val state: StateFlow<FullscreenPlayerUiState> = combine(
        coreState,
        quickMenuVisible,
    ) { core, menuVisible ->
        FullscreenPlayerUiState(
            camera = core.camera,
            audioMode = core.audioMode,
            streamQuality = core.streamQuality,
            showInfo = core.showInfo,
            transmissionMode = core.transmissionMode,
            quickMenuVisible = menuVisible,
            showQuickMenuHint = core.showQuickMenuHint && !menuVisible,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FullscreenPlayerUiState(),
    )

    fun open(camera: Camera) {
        this.camera.value = camera
        audioOverride.value = null
        streamQuality.value = StreamQuality.HD
        transmissionModeOverride.value = null
        quickMenuVisible.value = false
    }

    fun showQuickMenu() {
        quickMenuVisible.value = true
    }

    fun dismissQuickMenu() {
        quickMenuVisible.value = false
    }

    fun markQuickMenuHintSeen() {
        viewModelScope.launch {
            settingsRepository.setFullscreenQuickMenuHintSeen(true)
        }
    }

    fun toggleAudio() {
        audioOverride.value = when (state.value.audioMode) {
            AudioMode.Enabled -> AudioMode.Disabled
            AudioMode.Disabled -> AudioMode.Enabled
        }
    }

    fun toggleStreamQuality() {
        streamQuality.value = when (streamQuality.value) {
            StreamQuality.HD -> StreamQuality.SD
            StreamQuality.SD -> StreamQuality.HD
        }
    }

    fun toggleInfo() {
        viewModelScope.launch {
            settingsRepository.setShowFullscreenInfo(!state.value.showInfo)
        }
    }

    fun toggleTransmissionMode() {
        transmissionModeOverride.value = state.value.transmissionMode.next()
    }
}
