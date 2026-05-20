package com.sentinela.camtv.ui.mosaic

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sentinela.camtv.player.CameraStreamRequest
import com.sentinela.camtv.ui.player.RtspPlayerSurface
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RtspCameraTile(
    request: CameraStreamRequest,
    rtspUrl: String,
    showPlayerInfo: Boolean,
    selectedForReorder: Boolean,
    requestInitialFocus: Boolean,
    focusEnabled: Boolean,
    showFocusIndicator: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    var confirmKeyLongClickJob by remember { mutableStateOf<Job?>(null) }
    var confirmKeyLongClickHandled by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(requestInitialFocus, focusEnabled) {
        if (requestInitialFocus && focusEnabled) {
            focusRequester.requestFocus()
        }
    }

    val showFocusedBorder = focused && focusEnabled && showFocusIndicator

    DisposableEffect(Unit) {
        onDispose {
            confirmKeyLongClickJob?.cancel()
        }
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (!focusEnabled || onLongClick == null || !keyEvent.key.isConfirmKey()) {
                    return@onPreviewKeyEvent false
                }

                when (keyEvent.type) {
                    KeyEventType.KeyDown -> {
                        if (confirmKeyLongClickJob == null && !confirmKeyLongClickHandled) {
                            confirmKeyLongClickJob = coroutineScope.launch {
                                delay(CONFIRM_KEY_LONG_PRESS_DELAY_MS)
                                confirmKeyLongClickHandled = true
                                onLongClick()
                            }
                        }
                        confirmKeyLongClickHandled
                    }

                    KeyEventType.KeyUp -> {
                        val handled = confirmKeyLongClickHandled
                        confirmKeyLongClickJob?.cancel()
                        confirmKeyLongClickJob = null
                        confirmKeyLongClickHandled = false
                        handled
                    }

                    else -> false
                }
            }
            .border(
                width = if (showFocusedBorder || selectedForReorder) 4.dp else 1.dp,
                color = when {
                    selectedForReorder -> Color(0xFFFFD166)
                    showFocusedBorder -> Color(0xFF27D3FF)
                    else -> Color(0xFF375866)
                },
            )
            .onFocusChanged { focusState ->
                focused = focusState.isFocused
            }
            .combinedClickable(
                enabled = focusEnabled,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .focusable(enabled = focusEnabled),
    ) {
        RtspPlayerSurface(
            request = request,
            rtspUrl = rtspUrl,
            showPlayerInfo = showPlayerInfo,
            modifier = Modifier.fillMaxSize(),
        )

        if (request.camera.hasAuthenticationFailure) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xCC000000))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                BasicText(
                    text = "Bloqueado",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

private const val CONFIRM_KEY_LONG_PRESS_DELAY_MS = 1_200L

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
