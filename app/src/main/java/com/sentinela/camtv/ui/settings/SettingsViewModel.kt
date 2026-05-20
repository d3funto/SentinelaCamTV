package com.sentinela.camtv.ui.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.BuildConfig
import com.sentinela.camtv.data.update.AppUpdateInstaller
import com.sentinela.camtv.data.update.AvailableUpdate
import com.sentinela.camtv.data.update.DownloadedUpdate
import com.sentinela.camtv.data.update.UpdateCheckResult
import com.sentinela.camtv.data.update.UpdateRepository
import com.sentinela.camtv.logging.LogRepository
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val exportMessage: String? = null,
    val updateMessage: String? = null,
    val checkingForUpdate: Boolean = false,
    val downloadingUpdate: Boolean = false,
    val availableUpdate: AvailableUpdate? = null,
    val downloadedUpdate: DownloadedUpdate? = null,
    val versionName: String = BuildConfig.VERSION_NAME,
    val license: String = "GPL-3.0-or-later",
    val githubUrl: String = "https://github.com/d3funto/SentinelaCamTV",
)

class SettingsViewModel(
    private val logRepository: LogRepository,
    private val updateRepository: UpdateRepository,
    private val appUpdateInstaller: AppUpdateInstaller,
) : ViewModel() {
    private val exportMessage = MutableStateFlow<String?>(null)
    private val updateState = MutableStateFlow(UpdateUiState())

    val state: StateFlow<SettingsUiState> = combine(
        exportMessage,
        updateState,
    ) { export, update ->
        SettingsUiState(
            exportMessage = export,
            updateMessage = update.message,
            checkingForUpdate = update.checking,
            downloadingUpdate = update.downloading,
            availableUpdate = update.availableUpdate,
            downloadedUpdate = update.downloadedUpdate,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun exportSupportLogs() {
        exportFile { logRepository.exportSupportLogs() }
    }

    fun exportCrashReport() {
        exportFile { logRepository.exportCrashReport() }
    }

    fun clearExportMessage() {
        exportMessage.value = null
    }

    fun checkForUpdate() {
        if (updateState.value.checking || updateState.value.downloading) return

        viewModelScope.launch {
            updateState.value = UpdateUiState(
                checking = true,
                message = "Buscando atualização...",
            )
            updateRepository.checkForUpdate(
                currentVersionName = BuildConfig.VERSION_NAME,
                supportedAbis = Build.SUPPORTED_ABIS.toList(),
            ).fold(
                onSuccess = { result ->
                    updateState.value = when (result) {
                        is UpdateCheckResult.Available -> UpdateUiState(
                            message = "Versão ${result.update.versionName} disponível.",
                            availableUpdate = result.update,
                        )

                        UpdateCheckResult.UpToDate -> UpdateUiState(
                            message = "Você já está na versão mais recente.",
                        )
                    }
                },
                onFailure = { error ->
                    updateState.value = UpdateUiState(
                        message = "Falha ao buscar atualização: ${error.message ?: "erro desconhecido"}",
                    )
                },
            )
        }
    }

    fun downloadUpdate() {
        val update = updateState.value.availableUpdate ?: return
        if (updateState.value.downloading) return

        viewModelScope.launch {
            updateState.value = updateState.value.copy(
                downloading = true,
                message = "Baixando atualização...",
            )
            updateRepository.downloadUpdate(update).fold(
                onSuccess = { downloaded ->
                    updateState.value = UpdateUiState(
                        message = "Atualização baixada. Abrindo instalador...",
                        availableUpdate = update,
                        downloadedUpdate = downloaded,
                    )
                    openInstaller(downloaded)
                },
                onFailure = { error ->
                    updateState.value = updateState.value.copy(
                        downloading = false,
                        message = "Falha ao baixar atualização: ${error.message ?: "erro desconhecido"}",
                    )
                },
            )
        }
    }

    fun installDownloadedUpdate() {
        val downloaded = updateState.value.downloadedUpdate ?: return
        openInstaller(downloaded)
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

    private fun openInstaller(downloaded: DownloadedUpdate) {
        appUpdateInstaller.openInstaller(downloaded).fold(
            onSuccess = {
                updateState.value = updateState.value.copy(
                    downloading = false,
                    message = "Instalador aberto. Confirme a atualização no Android.",
                )
            },
            onFailure = { error ->
                updateState.value = updateState.value.copy(
                    downloading = false,
                    message = error.message ?: "Falha ao abrir o instalador.",
                )
            },
        )
    }
}

private data class UpdateUiState(
    val message: String? = null,
    val checking: Boolean = false,
    val downloading: Boolean = false,
    val availableUpdate: AvailableUpdate? = null,
    val downloadedUpdate: DownloadedUpdate? = null,
)
