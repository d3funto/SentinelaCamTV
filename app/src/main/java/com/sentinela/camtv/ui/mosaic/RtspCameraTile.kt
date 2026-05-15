package com.sentinela.camtv.ui.mosaic

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.ui.PlayerView
import com.sentinela.camtv.config.SHOW_CAMERA_LABELS
import com.sentinela.camtv.config.SHOW_PLAYER_DEBUG_INFO
import com.sentinela.camtv.player.buildMosaicRtspPlayer

@Composable
fun RtspCameraTile(
    name: String,
    rtspUrl: String,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Preparando RTSP...") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var videoInfo by remember { mutableStateOf("") }

    val context = LocalContext.current

    val player = remember(rtspUrl) {
        buildMosaicRtspPlayer(
            context = context,
            rtspUrl = rtspUrl,
        )
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                status = when (playbackState) {
                    Player.STATE_IDLE -> "Estado: parado"
                    Player.STATE_BUFFERING -> "Estado: conectando/buffer..."
                    Player.STATE_READY -> "Estado: pronto"
                    Player.STATE_ENDED -> "Estado: finalizado"
                    else -> "Estado: $playbackState"
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    status = "Estado: reproduzindo"
                    errorText = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                errorText = "${error.errorCodeName}: ${error.message ?: "erro sem mensagem"}"
                status = "Estado: erro"
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoInfo = "${videoSize.width}x${videoSize.height}"
                }
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(
                width = if (focused) 4.dp else 1.dp,
                color = if (focused) Color(0xFF27D3FF) else Color(0xFF375866),
                shape = RoundedCornerShape(16.dp),
            )
            .onFocusChanged { focusState ->
                focused = focusState.isFocused
            }
            .focusable(),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    isFocusable = false
                    isFocusableInTouchMode = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            update = { playerView ->
                playerView.player = player
            },
        )

        if (SHOW_CAMERA_LABELS) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color(0x99000000))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                BasicText(
                    text = if (SHOW_PLAYER_DEBUG_INFO && videoInfo.isNotBlank()) {
                        "$name - $videoInfo"
                    } else {
                        name
                    },
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }

        if (SHOW_PLAYER_DEBUG_INFO) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color(0x99000000))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                BasicText(
                    text = errorText ?: status,
                    style = TextStyle(
                        color = if (errorText == null) Color(0xFF9CCEDB) else Color(0xFFFF7777),
                        fontSize = 12.sp,
                    ),
                )
            }
        }
    }
}
