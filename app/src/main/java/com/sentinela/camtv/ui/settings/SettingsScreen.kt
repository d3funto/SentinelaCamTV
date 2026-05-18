package com.sentinela.camtv.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.sentinela.camtv.ui.common.BodyText
import com.sentinela.camtv.ui.common.ScreenTitle
import com.sentinela.camtv.ui.common.SectionTitle
import com.sentinela.camtv.ui.common.SentinelaScreen
import com.sentinela.camtv.ui.labels.activationLabel
import com.sentinela.camtv.ui.labels.statusLabel
import com.sentinela.camtv.ui.labels.transmissionModeLabel

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
    onOpenHome: () -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    SentinelaScreen {
        Column(
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            ScreenTitle("Ajustes")

            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("Reprodução")
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
                        label = "Áudio na tela cheia: ${statusLabel(state.preferences.fullscreenAudioEnabled)}",
                        onClick = onToggleFullscreenAudio,
                    )
                    SettingsActionButton(
                        label = "Modo padrão: ${transmissionModeLabel(state.preferences.globalTransmissionMode)}",
                        onClick = onToggleTransmissionMode,
                    )
                    SettingsActionButton(
                        label = "Iniciar ao ligar TV/Box: ${statusLabel(state.preferences.autoStartOnBoot)}",
                        onClick = onToggleAutoStartOnBoot,
                    )
                }

                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("Suporte")
                    SettingsActionButton(
                        label = "Exportar logs para suporte",
                        onClick = onExportSupportLogs,
                    )
                    SettingsActionButton(
                        label = "Exportar relatório de erros",
                        onClick = onExportCrashReport,
                    )
                    SettingsActionButton(
                        label = "Ir para início",
                        onClick = onOpenHome,
                    )

                    SectionTitle("Sobre o app")
                    BodyText("Sentinela Cam TV ${state.versionName}")
                    BodyText(state.license)
                    BodyText(state.githubUrl)

                    state.exportMessage?.let { message ->
                        BodyText(message)
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

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
