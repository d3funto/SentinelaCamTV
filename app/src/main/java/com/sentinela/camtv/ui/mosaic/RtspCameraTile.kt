package com.sentinela.camtv.ui.mosaic

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sentinela.camtv.player.CameraStreamRequest
import com.sentinela.camtv.ui.player.RtspPlayerSurface

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
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestInitialFocus, focusEnabled) {
        if (requestInitialFocus && focusEnabled) {
            focusRequester.requestFocus()
        }
    }

    val showFocusedBorder = focused && focusEnabled && showFocusIndicator

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
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
            .clickable(
                enabled = focusEnabled,
                onClick = onClick,
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
