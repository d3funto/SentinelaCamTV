package com.sentinela.camtv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource

@OptIn(UnstableApi::class)
fun buildRtspPlayer(
    context: Context,
    rtspUrl: String,
    streamConfig: PlayerStreamConfig,
): ExoPlayer {
    val bufferPreset = streamConfig.bufferPreset
    val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            bufferPreset.minBufferMs,
            bufferPreset.maxBufferMs,
            bufferPreset.bufferForPlaybackMs,
            bufferPreset.bufferAfterRebufferMs,
        )
        .build()

    val renderersFactory = DefaultRenderersFactory(context)
        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        .setEnableDecoderFallback(streamConfig.enableDecoderFallback)

    val rtspMediaSource = RtspMediaSource.Factory()
        .setTimeoutMs(streamConfig.rtspTimeoutMs)
        .setForceUseRtpTcp(streamConfig.transportMode == RtspTransportMode.TcpOnly)
        .createMediaSource(MediaItem.fromUri(rtspUrl))

    return ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .setRenderersFactory(renderersFactory)
        .build()
        .apply {
            applyAudioMode(streamConfig.audioMode)
            setMediaSource(rtspMediaSource)
            prepare()
            playWhenReady = true
        }
}

fun ExoPlayer.applyAudioMode(audioMode: AudioMode) {
    val audioDisabled = audioMode == AudioMode.Disabled

    volume = if (audioDisabled) 0f else 1f
    trackSelectionParameters = trackSelectionParameters
        .buildUpon()
        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, audioDisabled)
        .build()
}
