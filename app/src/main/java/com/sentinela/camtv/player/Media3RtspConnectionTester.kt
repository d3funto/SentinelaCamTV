package com.sentinela.camtv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

class Media3RtspConnectionTester(
    context: Context,
    private val timeoutMs: Long = 6_000L,
) : RtspConnectionTester {
    private val appContext = context.applicationContext

    @OptIn(UnstableApi::class)
    override suspend fun test(rtspUrl: String): RtspConnectionTestResult =
        withContext(Dispatchers.Main.immediate) {
            var player: ExoPlayer? = null
            try {
                withTimeout(timeoutMs) {
                    suspendCancellableCoroutine<RtspConnectionTestResult> { continuation ->
                        val streamConfig = defaultPlayerStreamConfig(
                            mode = PlayerMode.Fullscreen,
                            audioMode = AudioMode.Disabled,
                            transmissionMode = TransmissionMode.QUALIDADE,
                            transportMode = RtspTransportMode.TcpOnly,
                            enableDecoderFallback = true,
                        )
                        val bufferPreset = streamConfig.bufferPreset
                        val loadControl = DefaultLoadControl.Builder()
                            .setBufferDurationsMs(
                                bufferPreset.minBufferMs,
                                bufferPreset.maxBufferMs,
                                bufferPreset.bufferForPlaybackMs,
                                bufferPreset.bufferAfterRebufferMs,
                            )
                            .build()
                        val renderersFactory = DefaultRenderersFactory(appContext)
                            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                            .setEnableDecoderFallback(true)
                        val mediaSource = RtspMediaSource.Factory()
                            .setTimeoutMs(timeoutMs)
                            .setForceUseRtpTcp(true)
                            .createMediaSource(MediaItem.fromUri(rtspUrl))
                        val exoPlayer = ExoPlayer.Builder(appContext)
                            .setLoadControl(loadControl)
                            .setRenderersFactory(renderersFactory)
                            .build()
                        player = exoPlayer

                        val listener = object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_READY && continuation.isActive) {
                                    continuation.resume(RtspConnectionTestResult.Success)
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                if (continuation.isActive) {
                                    continuation.resume(
                                        RtspConnectionTestResult.Failure(
                                            PlayerErrorMapper.map(error, streamConfig.transportMode),
                                        ),
                                    )
                                }
                            }
                        }

                        continuation.invokeOnCancellation {
                            exoPlayer.removeListener(listener)
                            exoPlayer.release()
                        }
                        exoPlayer.addListener(listener)
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                            .build()
                        exoPlayer.volume = 0f
                        exoPlayer.setMediaSource(mediaSource)
                        exoPlayer.playWhenReady = false
                        exoPlayer.prepare()
                    }
                }
            } catch (_: TimeoutCancellationException) {
                RtspConnectionTestResult.Failure(PlayerConnectionState.Timeout)
            } catch (error: Throwable) {
                RtspConnectionTestResult.Failure(
                    PlayerErrorMapper.mapDiagnosticsText(
                        details = "${error.javaClass.simpleName}: ${error.message.orEmpty()}",
                        transportMode = RtspTransportMode.TcpOnly,
                        fallbackMessage = error.message ?: "Erro desconhecido",
                    ),
                )
            } finally {
                player?.release()
            }
        }
}
