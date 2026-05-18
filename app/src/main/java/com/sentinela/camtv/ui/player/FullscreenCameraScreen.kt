package com.sentinela.camtv.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sentinela.camtv.player.IntelbrasRtspUrlBuilder
import com.sentinela.camtv.ui.common.QuickMenu
import com.sentinela.camtv.ui.common.QuickMenuAction
import com.sentinela.camtv.ui.labels.activationLabel
import com.sentinela.camtv.ui.labels.audioLabel
import com.sentinela.camtv.ui.labels.streamQualityLabel
import com.sentinela.camtv.ui.labels.transmissionModeMenuLabel

@Composable
fun FullscreenCameraScreen(
    state: FullscreenPlayerUiState,
    rtspUrlBuilder: IntelbrasRtspUrlBuilder,
    onExit: () -> Unit,
    onShowQuickMenu: () -> Unit,
    onDismissQuickMenu: () -> Unit,
    onToggleAudio: () -> Unit,
    onToggleStreamQuality: () -> Unit,
    onToggleInfo: () -> Unit,
    onToggleTransmissionMode: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val request = state.streamRequest()
    val rtspUrl = remember(request, rtspUrlBuilder) {
        request?.let(rtspUrlBuilder::build)
    }

    BackHandler {
        if (state.quickMenuVisible) {
            onDismissQuickMenu()
        } else {
            onExit()
        }
    }

    LaunchedEffect(state.camera?.id, state.quickMenuVisible) {
        if (!state.quickMenuVisible) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (
                    !state.quickMenuVisible &&
                    keyEvent.type == KeyEventType.KeyUp &&
                    keyEvent.key.opensQuickMenu()
                ) {
                    onShowQuickMenu()
                    true
                } else {
                    false
                }
            }
            .focusable(enabled = !state.quickMenuVisible),
    ) {
        if (request == null || rtspUrl.isNullOrBlank()) {
            OpeningFullscreenMessage()
        } else {
            RtspPlayerSurface(
                request = request,
                rtspUrl = rtspUrl,
                showPlayerInfo = state.showInfo,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (state.quickMenuVisible) {
            FullscreenQuickMenu(
                state = state,
                onToggleAudio = onToggleAudio,
                onToggleStreamQuality = onToggleStreamQuality,
                onToggleInfo = onToggleInfo,
                onToggleTransmissionMode = onToggleTransmissionMode,
                onOpenHome = onOpenHome,
                onOpenSettings = onOpenSettings,
                onExitApp = onExitApp,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun FullscreenQuickMenu(
    state: FullscreenPlayerUiState,
    onToggleAudio: () -> Unit,
    onToggleStreamQuality: () -> Unit,
    onToggleInfo: () -> Unit,
    onToggleTransmissionMode: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuickMenu(
        actions = listOf(
            QuickMenuAction(audioLabel(state.audioMode), onToggleAudio),
            QuickMenuAction(streamQualityLabel(state.streamQuality), onToggleStreamQuality),
            QuickMenuAction("Informações: ${activationLabel(state.showInfo)}", onToggleInfo),
            QuickMenuAction(transmissionModeMenuLabel(state.transmissionMode), onToggleTransmissionMode),
            QuickMenuAction("Ir para início", onOpenHome),
            QuickMenuAction("Ir para ajustes", onOpenSettings),
            QuickMenuAction("Sair do app", onExitApp),
        ),
        modifier = modifier,
    )
}

@Composable
private fun OpeningFullscreenMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = "Abrindo câmera...",
            style = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

private fun Key.opensQuickMenu(): Boolean =
    this == Key.Enter ||
        this == Key.NumPadEnter ||
        this == Key.DirectionCenter ||
        this == Key.DirectionDown
