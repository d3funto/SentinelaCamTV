package com.sentinela.camtv.ui.mosaic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.sentinela.camtv.config.APP_PADDING_DP
import com.sentinela.camtv.config.AppDvrConfig
import com.sentinela.camtv.config.DvrConnectionConfig
import com.sentinela.camtv.config.TILE_GAP_DP
import com.sentinela.camtv.config.isConfigured
import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.domain.IntelbrasDvrChannel
import com.sentinela.camtv.player.IntelbrasRtspUrlBuilder
import com.sentinela.camtv.player.PlayerMode
import com.sentinela.camtv.player.streamRequestFor
import com.sentinela.camtv.ui.common.QuickMenu
import com.sentinela.camtv.ui.common.QuickMenuAction
import com.sentinela.camtv.ui.design.SentinelaTvColors
import com.sentinela.camtv.ui.labels.infoMenuLabel
import com.sentinela.camtv.ui.labels.transmissionModeMenuLabel
import com.sentinela.camtv.ui.player.FullscreenCameraScreen
import com.sentinela.camtv.ui.player.FullscreenPlayerViewModel
import kotlinx.coroutines.delay

private const val CAMERA_FOCUS_HIDE_DELAY_MS = 5_000L

@Composable
fun MosaicScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit,
    dvrConfig: DvrConnectionConfig = AppDvrConfig.intelbrasMhdx1004,
) {
    val mosaicViewModel: MosaicViewModel = viewModel(factory = viewModelFactory)
    val fullscreenViewModel: FullscreenPlayerViewModel = viewModel(factory = viewModelFactory)
    val state by mosaicViewModel.state.collectAsState()
    val fullscreenState by fullscreenViewModel.state.collectAsState()
    val rtspUrlBuilder = remember(dvrConfig) {
        IntelbrasRtspUrlBuilder(dvrConfig)
    }
    var showCameraFocusIndicator by remember { mutableStateOf(true) }
    var focusActivityToken by remember { mutableIntStateOf(0) }

    BackHandler {
        if (shouldReturnHomeOnMosaicBack(state)) {
            onOpenHome()
        } else {
            mosaicViewModel.onBackPressed()
        }
    }

    LaunchedEffect(focusActivityToken, showCameraFocusIndicator) {
        if (showCameraFocusIndicator) {
            delay(CAMERA_FOCUS_HIDE_DELAY_MS)
            showCameraFocusIndicator = false
        }
    }

    val fullscreenCamera = state.fullscreenCamera
    LaunchedEffect(fullscreenCamera?.id) {
        fullscreenCamera?.let(fullscreenViewModel::open)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071821))
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isDirectionalKey()) {
                    showCameraFocusIndicator = true
                    focusActivityToken += 1
                }
                false
            },
    ) {
        if (fullscreenCamera != null) {
            FullscreenCameraScreen(
                state = fullscreenState,
                rtspUrlBuilder = rtspUrlBuilder,
                onExit = {
                    fullscreenViewModel.dismissQuickMenu()
                    mosaicViewModel.closeFullscreen()
                },
                onShowQuickMenu = fullscreenViewModel::showQuickMenu,
                onDismissQuickMenu = fullscreenViewModel::dismissQuickMenu,
                onToggleAudio = fullscreenViewModel::toggleAudio,
                onToggleStreamQuality = fullscreenViewModel::toggleStreamQuality,
                onToggleInfo = fullscreenViewModel::toggleInfo,
                onToggleTransmissionMode = fullscreenViewModel::toggleTransmissionMode,
                onOpenHome = {
                    fullscreenViewModel.dismissQuickMenu()
                    mosaicViewModel.closeFullscreen()
                    onOpenHome()
                },
                onOpenSettings = {
                    fullscreenViewModel.dismissQuickMenu()
                    mosaicViewModel.closeFullscreen()
                    onOpenSettings()
                },
                onExitApp = onExitApp,
                onQuickMenuHintShown = fullscreenViewModel::markQuickMenuHintSeen,
                modifier = Modifier.fillMaxSize(),
            )
            return@Box
        }

        if (state.isLoading) {
            LoadingMosaicMessage()
            return@Box
        }

        if (state.cameras.isEmpty()) {
            EmptyMosaicMessage()
            return@Box
        }

        if (state.cameras.any { it.source is IntelbrasDvrChannel } && !dvrConfig.isConfigured()) {
            MissingDvrConfigMessage()
            return@Box
        }

        MosaicGrid(
            state = state,
            rtspUrlBuilder = rtspUrlBuilder,
            onCameraClick = mosaicViewModel::onCameraClick,
            onCameraLongClick = mosaicViewModel::requestCameraDeletion,
            tilesFocusable = !state.quickMenuVisible && state.cameraPendingDeletion == null,
            showFocusIndicator = showCameraFocusIndicator || state.reorderMode,
            modifier = Modifier.fillMaxSize(),
        )

        if (state.reorderMode) {
            ReorderHint(
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        if (state.quickMenuVisible) {
            MosaicQuickMenu(
                state = state,
                onExitApp = onExitApp,
                onToggleInfo = mosaicViewModel::toggleInfo,
                onStartReorder = mosaicViewModel::startReorderMode,
                onToggleTransmissionMode = mosaicViewModel::toggleTransmissionMode,
                onOpenHome = {
                    mosaicViewModel.dismissQuickMenu()
                    onOpenHome()
                },
                onOpenSettings = {
                    mosaicViewModel.dismissQuickMenu()
                    onOpenSettings()
                },
                modifier = Modifier.align(Alignment.Center),
            )
        }

        state.cameraPendingDeletion?.let { camera ->
            CameraDeletionDialog(
                cameraName = camera.name,
                onDismiss = mosaicViewModel::dismissCameraDeletion,
                onConfirm = mosaicViewModel::confirmCameraDeletion,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
fun SentinelaCamTvScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit,
) {
    MosaicScreen(
        viewModelFactory = viewModelFactory,
        onOpenHome = onOpenHome,
        onOpenSettings = onOpenSettings,
        onExitApp = onExitApp,
    )
}

@Composable
private fun MosaicGrid(
    state: MosaicUiState,
    rtspUrlBuilder: IntelbrasRtspUrlBuilder,
    onCameraClick: (Camera) -> Unit,
    onCameraLongClick: (Camera) -> Unit,
    tilesFocusable: Boolean,
    showFocusIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    val rows = remember(state.cameras) { state.cameras.mosaicRows() }
    Column(
        modifier = modifier.padding(APP_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(TILE_GAP_DP.dp),
    ) {
        rows.forEachIndexed { index, rowCameras ->
            MosaicCameraRow(
                cameras = rowCameras,
                state = state,
                rtspUrlBuilder = rtspUrlBuilder,
                onCameraClick = onCameraClick,
                onCameraLongClick = onCameraLongClick,
                tilesFocusable = tilesFocusable,
                showFocusIndicator = showFocusIndicator,
                rowWeight = if (state.cameras.size <= 5 && index == 1) 1.15f else 1f,
            )
        }
    }
}

private fun List<Camera>.mosaicRows(): List<List<Camera>> =
    if (size <= 5) {
        listOf(take(3), drop(3)).filter { row -> row.isNotEmpty() }
    } else {
        chunked(4)
    }

@Composable
private fun ColumnScope.MosaicCameraRow(
    cameras: List<Camera>,
    state: MosaicUiState,
    rtspUrlBuilder: IntelbrasRtspUrlBuilder,
    onCameraClick: (Camera) -> Unit,
    onCameraLongClick: (Camera) -> Unit,
    tilesFocusable: Boolean,
    showFocusIndicator: Boolean,
    rowWeight: Float,
) {
    if (cameras.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(rowWeight),
        horizontalArrangement = Arrangement.spacedBy(TILE_GAP_DP.dp),
    ) {
        cameras.forEach { camera ->
            key(camera.id) {
                val request = remember(camera, state.transmissionMode) {
                    camera.streamRequestFor(PlayerMode.Mosaic).copy(
                        transmissionMode = state.transmissionMode,
                    )
                }
                val rtspUrl = remember(request, rtspUrlBuilder) {
                    rtspUrlBuilder.build(request)
                }

                RtspCameraTile(
                    request = request,
                    rtspUrl = rtspUrl,
                    showPlayerInfo = state.showInfo,
                    selectedForReorder = state.selectedForSwapId == camera.id,
                    requestInitialFocus = camera.id == state.cameras.firstOrNull()?.id,
                    focusEnabled = tilesFocusable,
                    showFocusIndicator = showFocusIndicator,
                    onClick = {
                        onCameraClick(camera)
                    },
                    onLongClick = if (state.reorderMode) {
                        { onCameraLongClick(camera) }
                    } else {
                        null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun MosaicQuickMenu(
    state: MosaicUiState,
    onExitApp: () -> Unit,
    onToggleInfo: () -> Unit,
    onStartReorder: () -> Unit,
    onToggleTransmissionMode: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuickMenu(
        actions = listOf(
            QuickMenuAction("Sair do app", onExitApp),
            QuickMenuAction(infoMenuLabel(state.showInfo), onToggleInfo),
            QuickMenuAction("Editar mosaico", onStartReorder),
            QuickMenuAction(transmissionModeMenuLabel(state.transmissionMode), onToggleTransmissionMode),
            QuickMenuAction("Ir para início", onOpenHome),
            QuickMenuAction("Ir para suporte", onOpenSettings),
        ),
        modifier = modifier,
    )
}

@Composable
private fun ReorderHint(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(top = 14.dp)
            .widthIn(max = 860.dp)
            .background(
                color = SentinelaTvColors.panel.copy(alpha = 0.90f),
                shape = RoundedCornerShape(14.dp),
            )
            .border(
                width = 1.dp,
                color = SentinelaTvColors.panelBorder,
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(
            text = MosaicUiText.REORDER_HINT,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CameraDeletionDialog(
    cameraName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cancelFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        cancelFocusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .widthIn(min = 390.dp, max = 520.dp)
            .background(
                color = SentinelaTvColors.panel.copy(alpha = 0.96f),
                shape = RoundedCornerShape(18.dp),
            )
            .border(
                width = 1.dp,
                color = SentinelaTvColors.panelBorder,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(24.dp)
            .focusGroup(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = MosaicUiText.DELETE_CAMERA_CONFIRMATION,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = cameraName,
            color = SentinelaTvColors.mutedText,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DialogActionButton(
                label = "Cancelar",
                onClick = onDismiss,
                modifier = Modifier.focusRequester(cancelFocusRequester),
            )
            DialogActionButton(
                label = "Excluir",
                onClick = onConfirm,
            )
        }
    }
}

@Composable
private fun DialogActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 132.dp, minHeight = 48.dp)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key.isConfirmKey()) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .semantics { role = Role.Button }
            .background(
                color = SentinelaTvColors.control,
                shape = shape,
            )
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) SentinelaTvColors.controlFocused else SentinelaTvColors.panelBorder,
                shape = shape,
            )
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LoadingMosaicMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        MosaicMessageCard("Carregando câmeras...")
    }
}

@Composable
private fun EmptyMosaicMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        MosaicMessageCard("Nenhuma câmera cadastrada.")
    }
}

@Composable
private fun MissingDvrConfigMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        MosaicMessageCard(
            message = "Configure sentinela.dvr.host no local.properties para testar canais Intelbras.",
        )
    }
}

@Composable
private fun MosaicMessageCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .widthIn(min = 280.dp, max = 720.dp)
            .background(
                color = SentinelaTvColors.panel.copy(alpha = 0.94f),
                shape = RoundedCornerShape(16.dp),
            )
            .border(
                width = 1.dp,
                color = SentinelaTvColors.panelBorder,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 22.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun Key.isDirectionalKey(): Boolean =
    this == Key.DirectionLeft ||
        this == Key.DirectionRight ||
        this == Key.DirectionUp ||
        this == Key.DirectionDown

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
