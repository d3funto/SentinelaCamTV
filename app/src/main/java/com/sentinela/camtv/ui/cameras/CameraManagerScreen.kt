package com.sentinela.camtv.ui.cameras

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.sentinela.camtv.ui.common.BodyText
import com.sentinela.camtv.ui.common.ScreenTitle
import com.sentinela.camtv.ui.common.SectionTitle
import com.sentinela.camtv.ui.common.SentinelaScreen
import com.sentinela.onvif.DiscoveredOnvifDevice

@Composable
fun CameraManagerScreen(
    state: CameraManagerUiState,
    onDiscoverOnvif: () -> Unit,
    onDismissAuthDialog: () -> Unit,
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
            ScreenTitle("Gerenciar câmeras")

            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("Ações")
                    CameraManagerActionButton(
                        label = if (state.scanning) "Procurando ONVIF..." else "Buscar ONVIF na rede",
                        onClick = onDiscoverOnvif,
                        enabled = !state.scanning,
                        modifier = Modifier.focusRequester(focusRequester),
                    )
                    CameraManagerActionButton(
                        label = "Voltar",
                        onClick = onBack,
                    )

                    SectionTitle("Status")
                    BodyText(state.statusMessage ?: "Pronto")
                }

                Column(
                    modifier = Modifier.width(430.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("Câmeras salvas")
                    BodyText("${state.cameras.size} câmera(s)")
                    state.cameras.take(6).forEach { camera ->
                        BodyText(camera.name)
                    }

                    SectionTitle("Dispositivos ONVIF")
                    BodyText("${state.discoveredDevices.size} encontrado(s)")
                    state.discoveredDevices.take(4).forEach { device ->
                        BodyText(device.displayLabel())
                    }
                }
            }
        }

        state.authDialogMessage?.let { message ->
            AuthErrorDialog(
                message = message,
                onDismiss = onDismissAuthDialog,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun CameraManagerActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
        enabled = enabled,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun AuthErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(520.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Falha ao salvar câmera",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    }
}

private fun DiscoveredOnvifDevice.displayLabel(): String =
    scopes.firstOrNull()
        ?.substringAfterLast('/')
        ?.takeIf { it.isNotBlank() }
        ?: xAddrs.firstOrNull()
        ?: endpointReference

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
