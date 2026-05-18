package com.sentinela.camtv.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.sentinela.camtv.ui.labels.activationLabel
import com.sentinela.camtv.ui.labels.transmissionModeLabel
import com.sentinela.camtv.ui.theme.SentinelaBackground

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onToggleMosaicInfo: () -> Unit,
    onToggleFullscreenInfo: () -> Unit,
    onToggleFullscreenAudio: () -> Unit,
    onToggleTransmissionMode: () -> Unit,
    onToggleAutoStartOnBoot: () -> Unit,
    onExportSupportLogs: () -> Unit,
    onExportCrashReport: () -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelaBackground)
            .padding(horizontal = 56.dp, vertical = 40.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettingsSectionTitle("Reprodução")
                    SettingsActionButton(
                        label = "Informações no mosaico: ${activationLabel(state.preferences.showMosaicInfo)}",
                        onClick = onToggleMosaicInfo,
                        modifier = Modifier.focusRequester(focusRequester),
                    )
                    SettingsActionButton(
                        label = "Informações na tela cheia: ${activationLabel(state.preferences.showFullscreenInfo)}",
                        onClick = onToggleFullscreenInfo,
                    )
                    SettingsActionButton(
                        label = "Áudio na tela cheia: ${settingsStatusLabel(state.preferences.fullscreenAudioEnabled)}",
                        onClick = onToggleFullscreenAudio,
                    )
                    SettingsActionButton(
                        label = "Modo padrão: ${transmissionModeLabel(state.preferences.globalTransmissionMode)}",
                        onClick = onToggleTransmissionMode,
                    )
                    SettingsActionButton(
                        label = "Iniciar ao ligar TV/Box: ${settingsStatusLabel(state.preferences.autoStartOnBoot)}",
                        onClick = onToggleAutoStartOnBoot,
                    )
                }

                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettingsSectionTitle("Suporte")
                    SettingsActionButton(
                        label = "Exportar logs para suporte",
                        onClick = onExportSupportLogs,
                    )
                    SettingsActionButton(
                        label = "Exportar relatório de erros",
                        onClick = onExportCrashReport,
                    )
                    SettingsActionButton(
                        label = "Voltar",
                        onClick = onBack,
                    )

                    SettingsSectionTitle("Sobre o app")
                    StatusLine("Sentinela Cam TV ${state.versionName}")
                    StatusLine(state.license)
                    StatusLine(state.githubUrl)

                    state.exportMessage?.let { message ->
                        StatusLine(message)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key.isConfirmKey()) {
                    onClick()
                    true
                } else {
                    false
                }
            },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun StatusLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

private fun settingsStatusLabel(enabled: Boolean): String =
    if (enabled) "ativado" else "desativado"

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
