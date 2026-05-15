package com.sentinela.camtv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
fun buildMosaicRtspPlayer(
    context: Context,
    rtspUrl: String,
): ExoPlayer {
    val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            MosaicPlayerDefaults.MIN_BUFFER_MS,
            MosaicPlayerDefaults.MAX_BUFFER_MS,
            MosaicPlayerDefaults.BUFFER_FOR_PLAYBACK_MS,
            MosaicPlayerDefaults.BUFFER_AFTER_REBUFFER_MS,
        )
        .build()

    val renderersFactory = DefaultRenderersFactory(context)
        .setEnableDecoderFallback(true)

    return ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .setRenderersFactory(renderersFactory)
        .build()
        .apply {
            volume = 0f

            trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                .build()

            setMediaItem(MediaItem.fromUri(rtspUrl))
            prepare()
            playWhenReady = true
        }
}
