package com.sentinela.camtv.ui.player

import android.os.SystemClock
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.sentinela.camtv.player.AudioMode
import com.sentinela.camtv.player.CameraStreamRequest
import com.sentinela.camtv.player.PlayerConnectionState
import com.sentinela.camtv.player.PlayerErrorMapper
import com.sentinela.camtv.player.PlayerReconnectPolicy
import com.sentinela.camtv.player.ReconnectBackoffPolicy
import com.sentinela.camtv.player.RtspTransportMode
import com.sentinela.camtv.player.applyAudioMode
import com.sentinela.camtv.player.buildRtspPlayer
import com.sentinela.camtv.player.defaultPlayerStreamConfig
import com.sentinela.camtv.player.defaultTransportMode
import com.sentinela.camtv.player.isTerminalError
import com.sentinela.camtv.player.shortMessage
import com.sentinela.camtv.player.statusText
import kotlinx.coroutines.delay
import timber.log.Timber

private const val PLAYER_LOG_TAG = "SentinelaPlayer"

@Composable
fun RtspPlayerSurface(
    request: CameraStreamRequest,
    rtspUrl: String,
    showPlayerInfo: Boolean,
    modifier: Modifier = Modifier,
) {
    var connectionState by remember(rtspUrl, request.subtype, request.audioMode, request.transmissionMode) {
        mutableStateOf<PlayerConnectionState>(PlayerConnectionState.Connecting)
    }
    var videoInfo by remember(rtspUrl, request.subtype) { mutableStateOf("") }
    var reconnectAttempt by remember(rtspUrl, request.subtype, request.transmissionMode) { mutableIntStateOf(0) }
    var reconnectGeneration by remember(rtspUrl, request.subtype, request.transmissionMode) { mutableIntStateOf(0) }
    var reconnectToken by remember(rtspUrl, request.subtype, request.transmissionMode) { mutableIntStateOf(0) }
    var playerEnabled by remember(rtspUrl, request.subtype, request.transmissionMode) { mutableStateOf(true) }
    var consecutiveFailures by remember(rtspUrl, request.subtype, request.transmissionMode) { mutableIntStateOf(0) }
    var decoderFallbackEnabled by remember(
        rtspUrl,
        request.subtype,
        request.transmissionMode,
        request.enableDecoderFallback,
    ) {
        mutableStateOf(request.enableDecoderFallback)
    }
    var transportMode by remember(rtspUrl, request.subtype, request.transmissionMode) {
        mutableStateOf(request.transmissionMode.defaultTransportMode())
    }

    val context = LocalContext.current
    val backoffPolicy = remember { ReconnectBackoffPolicy() }
    val streamConfig = remember(
        request.mode,
        request.audioMode,
        request.transmissionMode,
        transportMode,
        decoderFallbackEnabled,
    ) {
        defaultPlayerStreamConfig(
            mode = request.mode,
            audioMode = request.audioMode,
            transmissionMode = request.transmissionMode,
            transportMode = transportMode,
            enableDecoderFallback = decoderFallbackEnabled,
        )
    }
    val playerStartedAtMs = remember(
        rtspUrl,
        request.mode,
        request.audioMode,
        request.transmissionMode,
        reconnectGeneration,
        transportMode,
        decoderFallbackEnabled,
    ) {
        SystemClock.elapsedRealtime()
    }

    val player = if (playerEnabled) {
        remember(
            rtspUrl,
            request.mode,
            request.audioMode,
            request.transmissionMode,
            reconnectGeneration,
            transportMode,
            decoderFallbackEnabled,
        ) {
            logRtspInfo(
                request = request,
                transportMode = transportMode,
                reconnectAttempt = reconnectAttempt,
                consecutiveFailures = consecutiveFailures,
                message = "criando player",
            )
            buildRtspPlayer(
                context = context,
                rtspUrl = rtspUrl,
                streamConfig = streamConfig,
            )
        }
    } else {
        null
    }

    LaunchedEffect(player, request.audioMode) {
        player?.applyAudioMode(request.audioMode)
    }

    fun scheduleReconnect(
        message: String,
        logMessage: String = message,
        throwable: Throwable? = null,
    ) {
        if (!playerEnabled || connectionState is PlayerConnectionState.Reconnecting) {
            return
        }

        val nextFailureCount = consecutiveFailures + 1
        val nextTransportMode = if (request.transmissionMode.defaultTransportMode() == RtspTransportMode.TcpOnly) {
            RtspTransportMode.TcpOnly
        } else {
            PlayerReconnectPolicy.transportModeForFailureCount(nextFailureCount)
        }

        logRtspWarning(
            request = request,
            transportMode = nextTransportMode,
            reconnectAttempt = reconnectAttempt,
            consecutiveFailures = nextFailureCount,
            message = "agendando reconexao: $logMessage",
            throwable = throwable,
        )

        consecutiveFailures = nextFailureCount
        transportMode = nextTransportMode
        connectionState = PlayerConnectionState.Reconnecting(message.ifBlank { null })
        playerEnabled = false
        reconnectToken += 1
    }

    LaunchedEffect(reconnectToken) {
        if (reconnectToken == 0) {
            return@LaunchedEffect
        }

        val delayMs = backoffPolicy.delayForAttempt(
            attempt = reconnectAttempt,
            jitterSeed = request.camera.id,
        )
        logRtspInfo(
            request = request,
            transportMode = transportMode,
            reconnectAttempt = reconnectAttempt,
            consecutiveFailures = consecutiveFailures,
            message = "aguardando ${delayMs}ms antes da reconexao",
        )
        delay(delayMs)
        reconnectAttempt += 1
        connectionState = PlayerConnectionState.Connecting
        reconnectGeneration += 1
        playerEnabled = true
    }

    LaunchedEffect(playerEnabled, reconnectGeneration, connectionState, request.mode) {
        if (!playerEnabled) {
            return@LaunchedEffect
        }

        val watchedState = connectionState
        val timeoutMs = PlayerReconnectPolicy.watchdogTimeoutFor(
            mode = request.mode,
            state = watchedState,
        ) ?: return@LaunchedEffect

        delay(timeoutMs)

        if (playerEnabled && connectionState == watchedState) {
            scheduleReconnect(
                "watchdog: ${watchedState.logLabel()} por ${timeoutMs}ms",
            )
        }
    }

    DisposableEffect(player) {
        if (player == null) {
            onDispose {}
        } else {
            val listener = object : Player.Listener {
                private var readyLogged = false

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> PlayerConnectionState.Buffering
                        Player.STATE_READY -> PlayerConnectionState.Playing
                        Player.STATE_ENDED -> {
                            scheduleReconnect("stream finalizado")
                            PlayerConnectionState.Reconnecting("Estado: stream finalizado")
                        }

                        Player.STATE_IDLE -> PlayerConnectionState.Connecting
                        else -> connectionState
                    }.also { nextState ->
                        if (nextState is PlayerConnectionState.Playing && !readyLogged) {
                            readyLogged = true
                            logRtspInfo(
                                request = request,
                                transportMode = transportMode,
                                reconnectAttempt = reconnectAttempt,
                                consecutiveFailures = consecutiveFailures,
                                message = "READY em ${SystemClock.elapsedRealtime() - playerStartedAtMs}ms",
                            )
                        }

                        if (connectionState !is PlayerConnectionState.Reconnecting) {
                            connectionState = nextState
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        logRtspInfo(
                            request = request,
                            transportMode = transportMode,
                            reconnectAttempt = reconnectAttempt,
                            consecutiveFailures = consecutiveFailures,
                            message = "reproducao ativa; resetando contadores",
                        )
                        reconnectAttempt = 0
                        consecutiveFailures = 0
                        connectionState = PlayerConnectionState.Playing
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    val mappedState = PlayerErrorMapper.map(error, transportMode)
                    if (
                        mappedState == PlayerConnectionState.UnsupportedCodec &&
                        !decoderFallbackEnabled
                    ) {
                        decoderFallbackEnabled = true
                    }
                    connectionState = mappedState
                    scheduleReconnect(
                        message = mappedState.statusText(),
                        logMessage = error.detailedMessage(),
                        throwable = error,
                    )
                }

                override fun onTracksChanged(tracks: Tracks) {
                    logTracks(request, tracks)
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
    }

    Box(
        modifier = modifier.background(Color.Black),
    ) {
        PlayerAndroidView(
            player = player,
            modifier = Modifier.fillMaxSize(),
        )

        if (showPlayerInfo) {
            PlayerInfoOverlay(
                cameraName = request.camera.name,
                videoInfo = videoInfo,
                connectionState = connectionState,
            )
        }
    }
}

private fun logRtspInfo(
    request: CameraStreamRequest,
    transportMode: RtspTransportMode,
    reconnectAttempt: Int,
    consecutiveFailures: Int,
    message: String,
) {
    Timber.tag(PLAYER_LOG_TAG).i(
        request.logPrefix(transportMode, reconnectAttempt, consecutiveFailures) +
            " $message",
    )
}

private fun logRtspWarning(
    request: CameraStreamRequest,
    transportMode: RtspTransportMode,
    reconnectAttempt: Int,
    consecutiveFailures: Int,
    message: String,
    throwable: Throwable? = null,
) {
    val fullMessage = request.logPrefix(transportMode, reconnectAttempt, consecutiveFailures) +
        " $message"
    if (throwable == null) {
        Timber.tag(PLAYER_LOG_TAG).w(fullMessage)
    } else {
        Timber.tag(PLAYER_LOG_TAG).w(throwable, fullMessage)
    }
}

private fun CameraStreamRequest.logPrefix(
    transportMode: RtspTransportMode,
    reconnectAttempt: Int,
    consecutiveFailures: Int,
): String =
    "camera=${camera.name} id=${camera.id} subtype=$subtype mode=$mode audio=$audioMode " +
        "transmission=$transmissionMode rtsp=$transportMode softwareFallback=$enableDecoderFallback " +
        "attempt=$reconnectAttempt failures=$consecutiveFailures"

private fun PlayerConnectionState.logLabel(): String = when (this) {
    PlayerConnectionState.Connecting -> "conectando"
    PlayerConnectionState.Buffering -> "buffer"
    PlayerConnectionState.Playing -> "reproduzindo"
    is PlayerConnectionState.Reconnecting -> "reconectando"
    PlayerConnectionState.NetworkOffline -> "rede offline"
    PlayerConnectionState.ConnectionRefused -> "conexão recusada"
    PlayerConnectionState.AuthenticationFailed -> "autenticação"
    PlayerConnectionState.Timeout -> "tempo esgotado"
    PlayerConnectionState.UnsupportedCodec -> "codec"
    PlayerConnectionState.UdpLikelyBlocked -> "udp instável"
    is PlayerConnectionState.UnknownError -> "erro"
}

private fun PlaybackException.detailedMessage(): String {
    val causeChain = generateSequence(cause) { throwable -> throwable.cause }
        .take(4)
        .joinToString(separator = " <- ") { throwable ->
            "${throwable.javaClass.simpleName}: ${throwable.message ?: "sem mensagem"}"
        }

    return if (causeChain.isBlank()) {
        shortMessage()
    } else {
        "${shortMessage()} | cause=$causeChain"
    }
}

private fun logTracks(
    request: CameraStreamRequest,
    tracks: Tracks,
) {
    if (tracks.groups.isEmpty()) {
        Timber.tag(PLAYER_LOG_TAG).i("${request.basicLogPrefix()} tracks=none")
        return
    }

    tracks.groups.forEachIndexed { groupIndex, group ->
        for (trackIndex in 0 until group.length) {
            val format = group.getTrackFormat(trackIndex)
            val details = buildList {
                add("type=${trackTypeLabel(group.type)}")
                add("mime=${format.sampleMimeType ?: "unknown"}")
                format.codecs?.let { add("codecs=$it") }
                if (format.width > 0 && format.height > 0) {
                    add("size=${format.width}x${format.height}")
                }
                if (format.sampleRate > 0) {
                    add("sampleRate=${format.sampleRate}")
                }
                if (format.channelCount > 0) {
                    add("channels=${format.channelCount}")
                }
                add("supported=${group.isTrackSupported(trackIndex)}")
                add("selected=${group.isTrackSelected(trackIndex)}")
            }.joinToString(separator = " ")

            Timber.tag(PLAYER_LOG_TAG).i(
                "${request.basicLogPrefix()} track[$groupIndex:$trackIndex] $details",
            )
        }
    }
}

private fun CameraStreamRequest.basicLogPrefix(): String =
    "camera=${camera.name} id=${camera.id} subtype=$subtype mode=$mode audio=$audioMode " +
        "transmission=$transmissionMode"

private fun trackTypeLabel(trackType: Int): String = when (trackType) {
    C.TRACK_TYPE_VIDEO -> "video"
    C.TRACK_TYPE_AUDIO -> "audio"
    C.TRACK_TYPE_TEXT -> "text"
    C.TRACK_TYPE_METADATA -> "metadata"
    else -> trackType.toString()
}

@Composable
private fun PlayerAndroidView(
    player: ExoPlayer?,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
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
}

@Composable
private fun BoxScope.PlayerInfoOverlay(
    cameraName: String,
    videoInfo: String,
    connectionState: PlayerConnectionState,
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .background(Color(0x99000000))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        BasicText(
            text = if (videoInfo.isNotBlank()) {
                "$cameraName - $videoInfo"
            } else {
                cameraName
            },
            style = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .background(Color(0x99000000))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        BasicText(
            text = connectionState.statusText(),
            style = TextStyle(
                color = if (connectionState.isTerminalError()) {
                    Color(0xFFFF7777)
                } else {
                    Color(0xFF9CCEDB)
                },
                fontSize = 12.sp,
            ),
        )
    }
}
