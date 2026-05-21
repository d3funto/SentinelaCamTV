package com.sentinela.camtv.ui.cameras

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.domain.IntelbrasDvrChannel
import com.sentinela.camtv.domain.OnvifCameraSource
import com.sentinela.camtv.domain.RtspCameraSource
import com.sentinela.camtv.ui.common.SentinelaScreen
import com.sentinela.camtv.ui.design.SentinelaTvColors
import com.sentinela.camtv.ui.design.SentinelaTvSize
import com.sentinela.onvif.DiscoveredOnvifDevice

private enum class CameraManagerTab(
    val label: String,
) {
    ONVIF("ONVIF"),
    RTSP("RTSP direto"),
    CONNECTED("Conectadas"),
}

internal const val CONNECTED_TAB_DESCRIPTION =
    "Confira as câmeras conectadas. Para trocar posições ou excluir, use Editar mosaico no menu rápido do mosaico."

private data class CameraManagerMetrics(
    val scale: Float,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val headerTabsGap: Dp,
    val onvifTabWidth: Dp,
    val rtspTabWidth: Dp,
    val connectedTabWidth: Dp,
    val tabHeight: Dp,
    val contentTopGap: Dp,
    val contentGap: Dp,
    val leftWidth: Dp,
    val rightWidth: Dp,
    val compactButtonWidth: Dp,
    val primaryButtonWidth: Dp,
    val secondaryButtonWidth: Dp,
    val fieldHeight: Dp,
    val statusCardWidth: Dp,
    val statusCardHeight: Dp,
    val rowCardHeight: Dp,
)

private class CameraManagerFocusRequesters {
    val onvifTab = FocusRequester()
    val rtspTab = FocusRequester()
    val connectedTab = FocusRequester()
    val firstContent = FocusRequester()
    val viewMosaic = FocusRequester()

    fun tab(tab: CameraManagerTab): FocusRequester =
        when (tab) {
            CameraManagerTab.ONVIF -> onvifTab
            CameraManagerTab.RTSP -> rtspTab
            CameraManagerTab.CONNECTED -> connectedTab
        }
}

@Composable
fun CameraManagerScreen(
    state: CameraManagerUiState,
    onDiscoverOnvif: () -> Unit,
    onSelectOnvifDevice: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSaveSelectedOnvifCamera: () -> Unit,
    onRtspNameChanged: (String) -> Unit,
    onRtspMainUrlChanged: (String) -> Unit,
    onRtspSubUrlChanged: (String) -> Unit,
    onRtspUsernameChanged: (String) -> Unit,
    onRtspPasswordChanged: (String) -> Unit,
    onCopyRtspMainUrlToSubUrl: () -> Unit,
    onConnectManualRtspCamera: () -> Unit,
    onDismissAuthDialog: () -> Unit,
    onOpenMosaic: () -> Unit,
    onBack: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(CameraManagerTab.ONVIF) }
    var textInputOpen by remember { mutableStateOf(false) }
    val focusRequesters = remember { CameraManagerFocusRequesters() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler(enabled = textInputOpen) {
        textInputOpen = false
        keyboardController?.hide()
    }

    BackHandler(enabled = !textInputOpen, onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequesters.onvifTab.requestFocus()
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val metrics = remember(maxWidth, maxHeight) {
            cameraManagerMetrics(maxWidth, maxHeight)
        }

        SentinelaScreen(
            horizontalPadding = metrics.horizontalPadding,
            verticalPadding = metrics.verticalPadding,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                CameraManagerHeader(
                    metrics = metrics,
                    focusRequesters = focusRequesters,
                    onTabSelected = { selectedTab = it },
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SentinelaTvColors.divider),
                )

                Spacer(Modifier.height(metrics.contentTopGap))

                when (selectedTab) {
                    CameraManagerTab.ONVIF -> OnvifTab(
                        state = state,
                        metrics = metrics,
                        focusRequesters = focusRequesters,
                        textInputOpen = textInputOpen,
                        onTextInputOpenChanged = { textInputOpen = it },
                        onDiscoverOnvif = onDiscoverOnvif,
                        onSelectOnvifDevice = onSelectOnvifDevice,
                        onUsernameChanged = onUsernameChanged,
                        onPasswordChanged = onPasswordChanged,
                        onSaveSelectedOnvifCamera = onSaveSelectedOnvifCamera,
                    )
                    CameraManagerTab.RTSP -> RtspTab(
                        state = state,
                        metrics = metrics,
                        focusRequesters = focusRequesters,
                        textInputOpen = textInputOpen,
                        onTextInputOpenChanged = { textInputOpen = it },
                        onRtspNameChanged = onRtspNameChanged,
                        onRtspMainUrlChanged = onRtspMainUrlChanged,
                        onRtspSubUrlChanged = onRtspSubUrlChanged,
                        onRtspUsernameChanged = onRtspUsernameChanged,
                        onRtspPasswordChanged = onRtspPasswordChanged,
                        onCopyRtspMainUrlToSubUrl = onCopyRtspMainUrlToSubUrl,
                        onConnectManualRtspCamera = onConnectManualRtspCamera,
                    )
                    CameraManagerTab.CONNECTED -> ConnectedTab(
                        state = state,
                        metrics = metrics,
                        focusRequesters = focusRequesters,
                        onOpenMosaic = onOpenMosaic,
                    )
                }
            }

            state.authDialogMessage?.let { message ->
                AuthErrorDialog(
                    message = message,
                    onDismiss = onDismissAuthDialog,
                    metrics = metrics,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun CameraManagerHeader(
    metrics: CameraManagerMetrics,
    focusRequesters: CameraManagerFocusRequesters,
    onTabSelected: (CameraManagerTab) -> Unit,
) {
    Column {
        TitleText("Adicione câmeras por ONVIF ou RTSP direto.", metrics)
        Spacer(Modifier.height(metrics.dp(14f)))
        Row(
            modifier = Modifier
                .focusGroup()
                .padding(bottom = metrics.dp(20f)),
            horizontalArrangement = Arrangement.spacedBy(metrics.headerTabsGap),
        ) {
            CameraManagerTab.entries.forEach { tab ->
                SentinelaTab(
                    label = tab.label,
                    width = metrics.tabWidth(tab),
                    height = metrics.tabHeight,
                    metrics = metrics,
                    modifier = Modifier
                        .focusRequester(focusRequesters.tab(tab))
                        .focusProperties {
                            down = focusRequesters.firstContent
                        },
                    onClick = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun OnvifTab(
    state: CameraManagerUiState,
    metrics: CameraManagerMetrics,
    focusRequesters: CameraManagerFocusRequesters,
    textInputOpen: Boolean,
    onTextInputOpenChanged: (Boolean) -> Unit,
    onDiscoverOnvif: () -> Unit,
    onSelectOnvifDevice: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSaveSelectedOnvifCamera: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(metrics.contentGap),
        ) {
            Column(
                modifier = Modifier.width(metrics.leftWidth),
            ) {
                SectionHeading("Descoberta ONVIF", metrics)
                SectionDescription("Procure dispositivos na rede local, selecione um e conecte com usuário e senha.", metrics)
                Spacer(Modifier.height(metrics.dp(14f)))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(metrics.dp(18f)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SentinelaActionButton(
                        label = if (state.scanning) "Procurando ONVIF..." else "Buscar ONVIF na rede",
                        onClick = onDiscoverOnvif,
                        enabled = !state.busy,
                        width = metrics.primaryButtonWidth,
                        height = metrics.dp(62f),
                        metrics = metrics,
                        modifier = Modifier.focusRequester(focusRequesters.firstContent),
                    )
                    SentinelaActionButton(
                        label = if (state.saving) "Conectando..." else "Conectar selecionado",
                        onClick = onSaveSelectedOnvifCamera,
                        enabled = !state.busy && state.selectedDevice != null,
                        width = metrics.secondaryButtonWidth,
                        height = metrics.dp(56f),
                        metrics = metrics,
                    )
                }
            }

            StatusCard(
                title = "Status",
                message = state.statusMessage ?: "Pronto para procurar dispositivos ONVIF na rede local.",
                width = metrics.statusCardWidth,
                height = metrics.statusCardHeight,
                metrics = metrics,
            )
        }

        Spacer(Modifier.height(metrics.dp(32f)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(metrics.contentGap),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.width(metrics.leftWidth),
            ) {
                SectionHeading("Credenciais", metrics)
                Spacer(Modifier.height(metrics.dp(10f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.dp(24f))) {
                    SentinelaTextField(
                        label = "Usuário",
                        value = state.username,
                        onValueChange = onUsernameChanged,
                        width = (metrics.leftWidth - metrics.dp(24f)) / 2,
                        height = metrics.fieldHeight,
                        metrics = metrics,
                        textInputOpen = textInputOpen,
                        onTextInputOpenChanged = onTextInputOpenChanged,
                    )
                    SentinelaTextField(
                        label = "Senha",
                        value = state.password,
                        onValueChange = onPasswordChanged,
                        width = (metrics.leftWidth - metrics.dp(24f)) / 2,
                        height = metrics.fieldHeight,
                        metrics = metrics,
                        password = true,
                        textInputOpen = textInputOpen,
                        onTextInputOpenChanged = onTextInputOpenChanged,
                    )
                }
            }
            InfoCard(
                message = "Usuário e senha são salvos de forma criptografada neste aparelho.",
                width = metrics.statusCardWidth,
                height = metrics.dp(76f),
                metrics = metrics,
            )
        }

        Spacer(Modifier.height(metrics.dp(30f)))

        SectionHeading("Dispositivos encontrados", metrics)
        Spacer(Modifier.height(metrics.dp(10f)))
        OnvifDevicesRow(
            devices = state.discoveredDevices,
            selectedDeviceKey = state.selectedDeviceKey,
            busy = state.busy,
            metrics = metrics,
            onSelectOnvifDevice = onSelectOnvifDevice,
        )
    }
}

@Composable
private fun RtspTab(
    state: CameraManagerUiState,
    metrics: CameraManagerMetrics,
    focusRequesters: CameraManagerFocusRequesters,
    textInputOpen: Boolean,
    onTextInputOpenChanged: (Boolean) -> Unit,
    onRtspNameChanged: (String) -> Unit,
    onRtspMainUrlChanged: (String) -> Unit,
    onRtspSubUrlChanged: (String) -> Unit,
    onRtspUsernameChanged: (String) -> Unit,
    onRtspPasswordChanged: (String) -> Unit,
    onCopyRtspMainUrlToSubUrl: () -> Unit,
    onConnectManualRtspCamera: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.contentGap),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(metrics.leftWidth),
        ) {
            SectionHeading("RTSP direto", metrics)
            SectionDescription("Use quando você já tem a URL RTSP da câmera, DVR ou NVR.", metrics)
            Spacer(Modifier.height(metrics.dp(22f)))
            SentinelaTextField(
                label = "Nome",
                value = state.rtspName,
                onValueChange = onRtspNameChanged,
                width = metrics.dp(390f),
                height = metrics.fieldHeight,
                metrics = metrics,
                placeholder = "Ex.: Portão",
                modifier = Modifier.focusRequester(focusRequesters.firstContent),
                textInputOpen = textInputOpen,
                onTextInputOpenChanged = onTextInputOpenChanged,
            )
            Spacer(Modifier.height(metrics.dp(16f)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(metrics.dp(18f)),
                verticalAlignment = Alignment.Bottom,
            ) {
                SentinelaTextField(
                    label = "URL RTSP principal",
                    value = state.rtspMainUrl,
                    onValueChange = onRtspMainUrlChanged,
                    width = metrics.leftWidth - metrics.compactButtonWidth - metrics.dp(18f),
                    height = metrics.fieldHeight,
                    metrics = metrics,
                    placeholder = "rtsp://192.168.0.10:554/...",
                    textInputOpen = textInputOpen,
                    onTextInputOpenChanged = onTextInputOpenChanged,
                )
                SentinelaActionButton(
                    label = "Colar embaixo",
                    onClick = onCopyRtspMainUrlToSubUrl,
                    enabled = !state.busy,
                    width = metrics.compactButtonWidth,
                    height = metrics.fieldHeight,
                    metrics = metrics,
                )
            }
            Spacer(Modifier.height(metrics.dp(16f)))
            SentinelaTextField(
                label = "URL RTSP secundária",
                value = state.rtspSubUrl,
                onValueChange = onRtspSubUrlChanged,
                width = metrics.leftWidth,
                height = metrics.fieldHeight,
                metrics = metrics,
                placeholder = "Opcional",
                textInputOpen = textInputOpen,
                onTextInputOpenChanged = onTextInputOpenChanged,
            )
        }

        Column(
            modifier = Modifier.width(metrics.rightWidth),
        ) {
            StatusCard(
                title = "Status",
                message = state.rtspStatusText(),
                width = metrics.statusCardWidth,
                height = metrics.dp(92f),
                metrics = metrics,
            )
            Spacer(Modifier.height(metrics.dp(34f)))
            SectionHeading("Credenciais", metrics)
            Spacer(Modifier.height(metrics.dp(16f)))
            SentinelaTextField(
                label = "Usuário",
                value = state.rtspUsername,
                onValueChange = onRtspUsernameChanged,
                width = metrics.rightWidth,
                height = metrics.fieldHeight,
                metrics = metrics,
                placeholder = "Opcional",
                textInputOpen = textInputOpen,
                onTextInputOpenChanged = onTextInputOpenChanged,
            )
            Spacer(Modifier.height(metrics.dp(16f)))
            SentinelaTextField(
                label = "Senha",
                value = state.rtspPassword,
                onValueChange = onRtspPasswordChanged,
                width = metrics.rightWidth,
                height = metrics.fieldHeight,
                metrics = metrics,
                placeholder = "Opcional",
                password = true,
                textInputOpen = textInputOpen,
                onTextInputOpenChanged = onTextInputOpenChanged,
            )
            Spacer(Modifier.height(metrics.dp(14f)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(metrics.dp(12f)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val connectButtonWidth = metrics.dp(166f)
                InfoCard(
                    message = "Credenciais são criptografadas neste aparelho.",
                    width = metrics.rightWidth - connectButtonWidth - metrics.dp(12f),
                    height = metrics.dp(58f),
                    metrics = metrics,
                )
                SentinelaActionButton(
                    label = if (state.rtspConnecting) "Conectando..." else "Conectar",
                    onClick = onConnectManualRtspCamera,
                    enabled = !state.busy,
                    width = connectButtonWidth,
                    height = metrics.dp(58f),
                    metrics = metrics,
                )
            }
        }
    }
}

@Composable
private fun ConnectedTab(
    state: CameraManagerUiState,
    metrics: CameraManagerMetrics,
    focusRequesters: CameraManagerFocusRequesters,
    onOpenMosaic: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.contentGap),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(metrics.leftWidth),
        ) {
            SectionHeading("Câmeras conectadas", metrics)
            SectionDescription(CONNECTED_TAB_DESCRIPTION, metrics)
            Spacer(Modifier.height(metrics.dp(20f)))
            if (state.cameras.isEmpty()) {
                CameraListItem(
                    title = "Nenhuma câmera conectada",
                    subtitle = "Use ONVIF ou RTSP direto para conectar uma câmera.",
                    height = metrics.rowCardHeight,
                    metrics = metrics,
                    modifier = Modifier
                        .focusRequester(focusRequesters.firstContent)
                        .focusProperties {
                            right = focusRequesters.viewMosaic
                        },
                )
            } else {
                state.cameras.sortedBy { it.position }.take(5).forEachIndexed { index, camera ->
                    CameraListItem(
                        title = camera.name,
                        subtitle = camera.connectedSubtitle(),
                        height = metrics.rowCardHeight,
                        metrics = metrics,
                        modifier = if (index == 0) {
                            Modifier
                                .focusRequester(focusRequesters.firstContent)
                                .focusProperties {
                                    right = focusRequesters.viewMosaic
                                }
                        } else {
                            Modifier.focusProperties {
                                right = focusRequesters.viewMosaic
                            }
                        },
                    )
                    if (index < state.cameras.size - 1 && index < 4) {
                        Spacer(Modifier.height(metrics.dp(8f)))
                    }
                }
            }
        }

        Column(
            modifier = Modifier.width(metrics.rightWidth),
        ) {
            StatusCard(
                title = "Status",
                message = cameraCountText(state.cameras.size, suffix = " conectadas."),
                width = metrics.statusCardWidth,
                height = metrics.dp(92f),
                metrics = metrics,
            )
            Spacer(Modifier.height(metrics.dp(34f)))
            SectionHeading("Resumo", metrics)
            Spacer(Modifier.height(metrics.dp(14f)))
            PanelText("ONVIF: ${state.cameras.count { it.source is OnvifCameraSource || it.source is IntelbrasDvrChannel }}", metrics)
            Spacer(Modifier.height(metrics.dp(12f)))
            PanelText("RTSP direto: ${state.cameras.count { it.source is RtspCameraSource }}", metrics)
            Spacer(Modifier.height(metrics.dp(88f)))
            SentinelaActionButton(
                label = "Ver câmeras",
                onClick = onOpenMosaic,
                width = metrics.dp(240f),
                height = metrics.dp(56f),
                metrics = metrics,
                modifier = Modifier
                    .focusRequester(focusRequesters.viewMosaic)
                    .focusProperties {
                        left = focusRequesters.firstContent
                        up = focusRequesters.connectedTab
                    },
            )
        }
    }
}

@Composable
private fun OnvifDevicesRow(
    devices: List<DiscoveredOnvifDevice>,
    selectedDeviceKey: String?,
    busy: Boolean,
    metrics: CameraManagerMetrics,
    onSelectOnvifDevice: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(metrics.dp(20f)),
        verticalAlignment = Alignment.Top,
    ) {
        if (devices.isEmpty()) {
            SelectableInfoItem(
                title = "Nenhum dispositivo encontrado",
                subtitle = "Use Buscar ONVIF na rede para atualizar a lista.",
                selected = false,
                width = metrics.leftWidth,
                height = metrics.dp(70f),
                metrics = metrics,
                enabled = false,
                onClick = {},
            )
        } else {
            devices.take(2).forEach { device ->
                val selected = selectedDeviceKey == device.stableKey()
                SelectableInfoItem(
                    title = device.displayLabel(),
                    subtitle = device.primaryXAddr().orEmpty(),
                    selected = selected,
                    width = (metrics.leftWidth - metrics.dp(20f)) / 2,
                    height = metrics.dp(70f),
                    metrics = metrics,
                    enabled = !busy,
                    onClick = { onSelectOnvifDevice(device.stableKey()) },
                )
            }
            if (devices.size == 1) {
                SelectableInfoItem(
                    title = "Nenhum outro dispositivo",
                    subtitle = "Use Buscar ONVIF na rede para atualizar a lista.",
                    selected = false,
                    width = (metrics.leftWidth - metrics.dp(20f)) / 2,
                    height = metrics.dp(70f),
                    metrics = metrics,
                    enabled = false,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun SentinelaTab(
    label: String,
    width: Dp,
    height: Dp,
    metrics: CameraManagerMetrics,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(metrics.dp(22f))

    Button(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .height(height)
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused) SentinelaTvSize.focusBorder else 1.dp,
                color = if (focused) MaterialTheme.colorScheme.primary else SentinelaTvColors.control,
                shape = shape,
            )
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key.isConfirmKey()) {
                    onClick()
                    true
                } else {
                    false
                }
            },
        scale = ButtonDefaults.scale(focusedScale = 1f),
        shape = ButtonDefaults.shape(
            shape = shape,
            focusedShape = shape,
            pressedShape = shape,
            disabledShape = shape,
        ),
        colors = tabColors(),
        contentPadding = PaddingValues(horizontal = metrics.dp(22f), vertical = 0.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = metrics.sp(20f),
                lineHeight = metrics.sp(24f),
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SentinelaActionButton(
    label: String,
    onClick: () -> Unit,
    width: Dp,
    height: Dp,
    metrics: CameraManagerMetrics,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(metrics.dp(24f))

    Button(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .height(height)
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused) SentinelaTvSize.focusBorder else 1.dp,
                color = if (focused) MaterialTheme.colorScheme.primary else SentinelaTvColors.control,
                shape = shape,
            )
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key.isConfirmKey()) {
                    onClick()
                    true
                } else {
                    false
                }
            },
        enabled = enabled,
        scale = ButtonDefaults.scale(focusedScale = 1f),
        shape = ButtonDefaults.shape(
            shape = shape,
            focusedShape = shape,
            pressedShape = shape,
            disabledShape = shape,
        ),
        colors = actionButtonColors(),
        contentPadding = PaddingValues(horizontal = metrics.dp(24f), vertical = 0.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = metrics.sp(22f),
                lineHeight = metrics.sp(26f),
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SentinelaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp,
    height: Dp,
    metrics: CameraManagerMetrics,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    password: Boolean = false,
    textInputOpen: Boolean,
    onTextInputOpenChanged: (Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(textInputOpen) {
        if (!textInputOpen) {
            editing = false
            keyboardController?.hide()
        }
    }

    LaunchedEffect(editing) {
        if (editing) {
            keyboardController?.show()
        }
    }

    Column(
        modifier = modifier.width(width),
        verticalArrangement = Arrangement.spacedBy(metrics.dp(4f)),
    ) {
        LabelText(label, metrics)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(SentinelaTvColors.field)
                .border(
                    width = if (focused) SentinelaTvSize.focusBorder else 1.dp,
                    color = if (focused) MaterialTheme.colorScheme.primary else SentinelaTvColors.fieldBorder,
                )
                .padding(horizontal = metrics.dp(14f)),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isBlank() && placeholder.isNotBlank()) {
                Text(
                    text = placeholder,
                    style = fieldTextStyle(metrics).copy(color = SentinelaTvColors.mutedText),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = fieldTextStyle(metrics),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (password) KeyboardType.Password else KeyboardType.Text,
                ),
                readOnly = !editing,
                visualTransformation = if (password) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        focused = focusState.isFocused
                        if (focusState.isFocused && !editing) {
                            keyboardController?.hide()
                        } else if (!focusState.isFocused) {
                            editing = false
                            onTextInputOpenChanged(false)
                            keyboardController?.hide()
                        }
                    }
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyUp && event.key.isConfirmKey() && !editing) {
                            editing = true
                            onTextInputOpenChanged(true)
                            true
                        } else {
                            false
                        }
                    },
            )
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    message: String,
    width: Dp,
    height: Dp,
    metrics: CameraManagerMetrics,
) {
    Column(
        modifier = Modifier
            .width(width)
            .heightIn(min = height)
            .background(SentinelaTvColors.panel, RoundedCornerShape(metrics.dp(10f)))
            .padding(horizontal = metrics.dp(18f), vertical = metrics.dp(14f)),
        verticalArrangement = Arrangement.spacedBy(metrics.dp(8f)),
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = metrics.sp(16f),
                lineHeight = metrics.sp(20f),
                fontWeight = FontWeight.Bold,
            ),
        )
        PanelText(message, metrics)
    }
}

@Composable
private fun InfoCard(
    message: String,
    width: Dp,
    height: Dp,
    metrics: CameraManagerMetrics,
) {
    Box(
        modifier = Modifier
            .width(width)
            .heightIn(min = height)
            .background(SentinelaTvColors.panel, RoundedCornerShape(metrics.dp(10f)))
            .padding(horizontal = metrics.dp(18f), vertical = metrics.dp(12f)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = metrics.sp(18f),
                lineHeight = metrics.sp(24f),
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun SelectableInfoItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    width: Dp,
    height: Dp,
    metrics: CameraManagerMetrics,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(metrics.dp(10f))

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .width(width)
            .height(height)
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused) SentinelaTvSize.focusBorder else 1.dp,
                color = if (focused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    if (selected) SentinelaTvColors.controlSelected else SentinelaTvColors.field
                },
                shape = shape,
            ),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        shape = ButtonDefaults.shape(
            shape = shape,
            focusedShape = shape,
            pressedShape = shape,
            disabledShape = shape,
        ),
        colors = listItemButtonColors(selected),
        contentPadding = PaddingValues(horizontal = metrics.dp(16f), vertical = metrics.dp(8f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = metrics.sp(21f),
                    lineHeight = metrics.sp(25f),
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = metrics.sp(15f),
                    lineHeight = metrics.sp(19f),
                    fontWeight = FontWeight.Normal,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CameraListItem(
    title: String,
    subtitle: String,
    height: Dp,
    metrics: CameraManagerMetrics,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(metrics.dp(9f))

    Button(
        onClick = {},
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused) SentinelaTvSize.focusBorder else 1.dp,
                color = if (focused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    SentinelaTvColors.field
                },
                shape = shape,
            ),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        shape = ButtonDefaults.shape(
            shape = shape,
            focusedShape = shape,
            pressedShape = shape,
            disabledShape = shape,
        ),
        colors = listItemButtonColors(selected = false),
        contentPadding = PaddingValues(horizontal = metrics.dp(18f), vertical = metrics.dp(10f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = metrics.sp(21f),
                    lineHeight = metrics.sp(25f),
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = metrics.sp(16f),
                    lineHeight = metrics.sp(20f),
                    fontWeight = FontWeight.Normal,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TitleText(
    text: String,
    metrics: CameraManagerMetrics,
) {
    Text(
        text = text,
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = metrics.sp(32f),
            lineHeight = metrics.sp(38f),
            fontWeight = FontWeight.Bold,
        ),
    )
}

@Composable
private fun SectionHeading(
    text: String,
    metrics: CameraManagerMetrics,
) {
    Text(
        text = text,
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = metrics.sp(26f),
            lineHeight = metrics.sp(31f),
            fontWeight = FontWeight.Bold,
        ),
    )
}

@Composable
private fun SectionDescription(
    text: String,
    metrics: CameraManagerMetrics,
) {
    Text(
        text = text,
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = metrics.sp(18f),
            lineHeight = metrics.sp(23f),
            fontWeight = FontWeight.Normal,
        ),
    )
}

@Composable
private fun LabelText(
    text: String,
    metrics: CameraManagerMetrics,
) {
    Text(
        text = text,
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = metrics.sp(17f),
            lineHeight = metrics.sp(21f),
            fontWeight = FontWeight.Bold,
        ),
    )
}

@Composable
private fun PanelText(
    text: String,
    metrics: CameraManagerMetrics,
) {
    Text(
        text = text,
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = metrics.sp(19f),
            lineHeight = metrics.sp(25f),
            fontWeight = FontWeight.Normal,
        ),
    )
}

@Composable
private fun AuthErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    metrics: CameraManagerMetrics,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(metrics.dp(520f))
            .background(SentinelaTvColors.panel, RoundedCornerShape(metrics.dp(10f)))
            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(metrics.dp(10f)))
            .padding(metrics.dp(24f)),
        verticalArrangement = Arrangement.spacedBy(metrics.dp(14f)),
    ) {
        SectionHeading("Falha ao conectar câmera", metrics)
        PanelText(message, metrics)
        SentinelaActionButton(
            label = "OK",
            onClick = onDismiss,
            width = metrics.dp(120f),
            height = metrics.dp(48f),
            metrics = metrics,
        )
    }
}

@Composable
private fun tabColors() =
    ButtonDefaults.colors(
        containerColor = SentinelaTvColors.control,
        contentColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = SentinelaTvColors.control,
        focusedContentColor = MaterialTheme.colorScheme.onSurface,
        pressedContainerColor = SentinelaTvColors.control,
        pressedContentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = SentinelaTvColors.control.copy(alpha = 0.38f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
    )

@Composable
private fun actionButtonColors() =
    ButtonDefaults.colors(
        containerColor = SentinelaTvColors.control,
        contentColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = SentinelaTvColors.control,
        focusedContentColor = MaterialTheme.colorScheme.onSurface,
        pressedContainerColor = SentinelaTvColors.control,
        pressedContentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = SentinelaTvColors.control.copy(alpha = 0.54f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
    )

@Composable
private fun listItemButtonColors(selected: Boolean) =
    ButtonDefaults.colors(
        containerColor = if (selected) SentinelaTvColors.controlSelected else SentinelaTvColors.field,
        contentColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = if (selected) SentinelaTvColors.controlSelected else SentinelaTvColors.field,
        focusedContentColor = MaterialTheme.colorScheme.onSurface,
        pressedContainerColor = if (selected) SentinelaTvColors.controlSelected else SentinelaTvColors.field,
        pressedContentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = SentinelaTvColors.field,
        disabledContentColor = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
        },
    )

@Composable
private fun fieldTextStyle(metrics: CameraManagerMetrics): TextStyle =
    TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = metrics.sp(18f),
        lineHeight = metrics.sp(23f),
        fontWeight = FontWeight.Normal,
    )

private fun cameraManagerMetrics(
    width: Dp,
    height: Dp,
): CameraManagerMetrics {
    val scale = minOf(width.value / 1280f, height.value / 720f)
        .coerceIn(0.65f, 1.0f)

    fun dp(px: Float): Dp = (px * scale).dp

    return CameraManagerMetrics(
        scale = scale,
        horizontalPadding = dp(76f),
        verticalPadding = dp(60f),
        headerTabsGap = dp(14f),
        onvifTabWidth = dp(126f),
        rtspTabWidth = dp(176f),
        connectedTabWidth = dp(170f),
        tabHeight = dp(50f),
        contentTopGap = dp(30f),
        contentGap = dp(28f),
        leftWidth = dp(705f),
        rightWidth = dp(390f),
        compactButtonWidth = dp(210f),
        primaryButtonWidth = dp(365f),
        secondaryButtonWidth = dp(300f),
        fieldHeight = dp(50f),
        statusCardWidth = dp(390f),
        statusCardHeight = dp(92f),
        rowCardHeight = dp(70f),
    )
}

private fun CameraManagerMetrics.dp(px: Float): Dp = (px * scale).dp

private fun CameraManagerMetrics.sp(px: Float) = (px * scale).sp

private fun CameraManagerMetrics.tabWidth(tab: CameraManagerTab): Dp =
    when (tab) {
        CameraManagerTab.ONVIF -> onvifTabWidth
        CameraManagerTab.RTSP -> rtspTabWidth
        CameraManagerTab.CONNECTED -> connectedTabWidth
    }

private fun CameraManagerUiState.rtspStatusText(): String {
    val message = statusMessage
    return when {
        rtspConnecting -> message ?: "Conectando RTSP..."
        message != null && message.isRtspStatusMessage() -> message
        else -> "Informe a URL RTSP e, se necessário, usuário e senha."
    }
}

private fun String.isRtspStatusMessage(): Boolean =
    contains("RTSP", ignoreCase = true) ||
        startsWith("Fluxo ") ||
        startsWith("Conectando Fluxo") ||
        startsWith("Informe usuário e senha")

private fun Camera.connectedSubtitle(): String =
    "${source.connectedLabel()} • ${streamLabel()}"

private fun Any.connectedLabel(): String =
    when (this) {
        is IntelbrasDvrChannel -> "ONVIF"
        is OnvifCameraSource -> "ONVIF"
        is RtspCameraSource -> "RTSP direto"
        else -> "Câmera"
    }

private fun Camera.streamLabel(): String =
    when (source) {
        is IntelbrasDvrChannel -> "stream principal e secundário"
        is OnvifCameraSource -> if (source.subRtspUrl.isNullOrBlank()) {
            "stream principal"
        } else {
            "stream principal e secundário"
        }
        is RtspCameraSource -> if (source.subRtspUrl.isNullOrBlank()) {
            "stream principal"
        } else {
            "stream principal e secundário"
        }
    }

private fun cameraCountText(
    count: Int,
    suffix: String = "",
): String =
    if (count == 1) {
        "1 câmera$suffix"
    } else {
        "$count câmeras$suffix"
    }

private fun DiscoveredOnvifDevice.primaryXAddr(): String? =
    xAddrs.firstOrNull { address -> address.startsWith("http", ignoreCase = true) }
        ?: xAddrs.firstOrNull()

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
