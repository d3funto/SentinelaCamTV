package com.sentinela.camtv.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.BuildConfig
import com.sentinela.camtv.logging.LogRepository
import com.sentinela.camtv.player.TransmissionMode
import com.sentinela.camtv.player.next
import com.sentinela.camtv.preferences.PlayerUiPreferences
import com.sentinela.camtv.preferences.SettingsRepository
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: PlayerUiPreferences = PlayerUiPreferences(),
    val exportMessage: String? = null,
    val versionName: String = BuildConfig.VERSION_NAME,
    val license: String = "GPL-3.0-or-later",
    val githubUrl: String = "https://github.com/d3funto/SentinelaCamTV",
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository,
) : ViewModel() {
    private val exportMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<SettingsUiState> = combine(
        settingsRepository.observePreferences(),
        exportMessage,
    ) { preferences, message ->
        SettingsUiState(
            preferences = preferences,
            exportMessage = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun toggleMosaicInfo() {
        viewModelScope.launch {
            settingsRepository.setShowMosaicInfo(!state.value.preferences.showMosaicInfo)
        }
    }

    fun toggleFullscreenInfo() {
        viewModelScope.launch {
            settingsRepository.setShowFullscreenInfo(!state.value.preferences.showFullscreenInfo)
        }
    }

    fun toggleFullscreenAudio() {
        viewModelScope.launch {
            settingsRepository.setFullscreenAudioEnabled(!state.value.preferences.fullscreenAudioEnabled)
        }
    }

    fun toggleAutoStartOnBoot() {
        viewModelScope.launch {
            settingsRepository.setAutoStartOnBoot(!state.value.preferences.autoStartOnBoot)
        }
    }

    fun toggleTransmissionMode() {
        viewModelScope.launch {
            settingsRepository.setGlobalTransmissionMode(state.value.preferences.globalTransmissionMode.next())
        }
    }

    fun setTransmissionMode(transmissionMode: TransmissionMode) {
        viewModelScope.launch {
            settingsRepository.setGlobalTransmissionMode(transmissionMode)
        }
    }

    fun exportSupportLogs() {
        exportFile { logRepository.exportSupportLogs() }
    }

    fun exportCrashReport() {
        exportFile { logRepository.exportCrashReport() }
    }

    fun clearExportMessage() {
        exportMessage.value = null
    }

    private fun exportFile(block: suspend () -> Result<File>) {
        viewModelScope.launch {
            exportMessage.value = "Exportando..."
            exportMessage.value = block()
                .fold(
                    onSuccess = { file -> "Arquivo gerado: ${file.absolutePath}" },
                    onFailure = { error -> "Falha ao exportar: ${error.message ?: "erro desconhecido"}" },
                )
        }
    }
}
