package com.sentinela.camtv.ui.cameras

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.sentinela.camtv.ui.common.BodyText
import com.sentinela.camtv.ui.common.SectionTitle
import com.sentinela.camtv.ui.common.SentinelaScreen
import com.sentinela.onvif.DiscoveredOnvifDevice

@Composable
fun CameraManagerScreen(
    state: CameraManagerUiState,
    onDiscoverOnvif: () -> Unit,
    onSelectOnvifDevice: (String) -> Unit,
    onManualOnvifAddressChanged: (String) -> Unit,
    onUseManualOnvifAddress: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSaveSelectedOnvifCamera: () -> Unit,
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
            modifier = Modifier.verticalScroll(rememberScrollState()),
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
                    CameraManagerActionButton(
                        label = if (state.scanning) "Procurando ONVIF..." else "Buscar ONVIF na rede",
                        onClick = onDiscoverOnvif,
                        enabled = !state.busy,
                        modifier = Modifier.focusRequester(focusRequester),
                    )
                    CameraManagerActionButton(
                        label = if (state.saving) "Salvando câmera ONVIF..." else "Salvar ONVIF selecionada",
                        onClick = onSaveSelectedOnvifCamera,
                        enabled = !state.busy && state.selectedDevice != null,
                    )
                    CameraManagerActionButton(
                        label = "Usar endereço informado",
                        onClick = onUseManualOnvifAddress,
                        enabled = !state.busy,
                    )

                    SectionTitle("Status")
                    BodyText(state.statusMessage ?: "Pronto")

                    SectionTitle("Endereço ONVIF manual")
                    CameraManagerTextField(
                        label = "IP ou URL do serviço ONVIF",
                        value = state.manualOnvifAddress,
                        onValueChange = onManualOnvifAddressChanged,
                    )

                    SectionTitle("Credenciais ONVIF")
                    CameraManagerTextField(
                        label = "Usuário",
                        value = state.username,
                        onValueChange = onUsernameChanged,
                    )
                    CameraManagerTextField(
                        label = "Senha",
                        value = state.password,
                        onValueChange = onPasswordChanged,
                        password = true,
                    )
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
                    state.discoveredDevices.take(6).forEach { device ->
                        CameraManagerActionButton(
                            label = if (state.selectedDeviceKey == device.stableKey()) {
                                "Selecionado: ${device.displayLabel()}"
                            } else {
                                device.displayLabel()
                            },
                            onClick = { onSelectOnvifDevice(device.stableKey()) },
                            enabled = !state.busy,
                        )
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
private fun CameraManagerTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    password: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (password) KeyboardType.Password else KeyboardType.Text,
            ),
            visualTransformation = if (password) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        )
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

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
