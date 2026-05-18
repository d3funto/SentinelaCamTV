package com.sentinela.camtv.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.sentinela.camtv.player.TransmissionMode
import com.sentinela.camtv.ui.labels.transmissionModeLabel
import com.sentinela.camtv.ui.theme.SentinelaBackground

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onToggleMosaicInfo: () -> Unit,
    onToggleFullscreenInfo: () -> Unit,
    onToggleFullscreenAudio: () -> Unit,
    onToggleTransmissionMode: () -> Unit,
    onSetTransmissionMode: (TransmissionMode) -> Unit,
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
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.displaySmall,
            )

            SettingsSwitchRow(
                title = "Informações no mosaico",
                checked = state.preferences.showMosaicInfo,
                onClick = onToggleMosaicInfo,
                modifier = Modifier.focusRequester(focusRequester),
            )

            SettingsSwitchRow(
                title = "Informações na tela cheia",
                checked = state.preferences.showFullscreenInfo,
                onClick = onToggleFullscreenInfo,
            )

            SettingsSwitchRow(
                title = "Áudio na tela cheia",
                checked = state.preferences.fullscreenAudioEnabled,
                onClick = onToggleFullscreenAudio,
            )

            SettingsSwitchRow(
                title = "Iniciar ao ligar TV/Box",
                checked = state.preferences.autoStartOnBoot,
                onClick = onToggleAutoStartOnBoot,
            )

            Button(onClick = onToggleTransmissionMode) {
                Text("Modo padrão: ${transmissionModeLabel(state.preferences.globalTransmissionMode)}")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSetTransmissionMode(TransmissionMode.MENOR_LATENCIA) }) {
                    Text("Menor latência")
                }
                Button(onClick = { onSetTransmissionMode(TransmissionMode.QUALIDADE) }) {
                    Text("Qualidade")
                }
            }

            Button(onClick = onExportSupportLogs) {
                Text("Exportar logs para suporte")
            }

            Button(onClick = onExportCrashReport) {
                Text("Exportar relatório de erros")
            }

            Text(
                text = "Sobre: Sentinela Cam TV ${state.versionName} | ${state.license}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state.githubUrl,
                style = MaterialTheme.typography.titleMedium,
            )

            state.exportMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Button(onClick = onBack) {
                Text("Voltar")
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .focusable(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = { onClick() },
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
