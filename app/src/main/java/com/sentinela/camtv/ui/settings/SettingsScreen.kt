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
import com.sentinela.camtv.ui.common.AppAboutFooter
import com.sentinela.camtv.ui.common.BodyText
import com.sentinela.camtv.ui.common.SectionTitle
import com.sentinela.camtv.ui.common.SentinelaScreen

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onExportSupportLogs: () -> Unit,
    onExportCrashReport: () -> Unit,
    onCheckForUpdate: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallDownloadedUpdate: () -> Unit,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettingsActionButton(
                        label = "Exportar logs para suporte",
                        onClick = onExportSupportLogs,
                        modifier = Modifier.focusRequester(focusRequester),
                    )
                    SettingsActionButton(
                        label = "Exportar logs de crashes",
                        onClick = onExportCrashReport,
                    )
                    SettingsActionButton(
                        label = if (state.checkingForUpdate) {
                            "Buscando atualização..."
                        } else {
                            "Buscar atualização"
                        },
                        onClick = onCheckForUpdate,
                        enabled = !state.checkingForUpdate && !state.downloadingUpdate,
                    )
                    state.availableUpdate?.let {
                        SettingsActionButton(
                            label = if (state.downloadingUpdate) {
                                "Baixando..."
                            } else {
                                "Baixar"
                            },
                            onClick = onDownloadUpdate,
                            enabled = !state.downloadingUpdate && !state.checkingForUpdate,
                        )
                    }
                    state.downloadedUpdate?.let {
                        SettingsActionButton(
                            label = "Instalar",
                            onClick = onInstallDownloadedUpdate,
                            enabled = !state.downloadingUpdate && !state.checkingForUpdate,
                        )
                    }
                    SettingsActionButton(
                        label = "Ir para início",
                        onClick = onOpenHome,
                    )
                }

                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("Suporte")
                    AppAboutFooter(
                        versionName = state.versionName,
                        license = state.license,
                        githubUrl = state.githubUrl,
                    )

                    state.exportMessage?.let { message ->
                        BodyText(message)
                    }
                    state.updateMessage?.let { message ->
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
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { keyEvent ->
                if (enabled && keyEvent.type == KeyEventType.KeyUp && keyEvent.key.isConfirmKey()) {
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
