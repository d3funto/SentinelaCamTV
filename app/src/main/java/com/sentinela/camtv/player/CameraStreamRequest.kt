package com.sentinela.camtv.player

import com.sentinela.camtv.config.FULLSCREEN_SUBTYPE
import com.sentinela.camtv.config.MOSAIC_SUBTYPE
import com.sentinela.camtv.domain.Camera

data class CameraStreamRequest(
    val camera: Camera,
    val subtype: Int,
    val mode: PlayerMode,
    val audioMode: AudioMode,
    val transmissionMode: TransmissionMode = TransmissionMode.MENOR_LATENCIA,
    val enableDecoderFallback: Boolean = false,
)

fun Camera.streamRequestFor(mode: PlayerMode): CameraStreamRequest = when (mode) {
    PlayerMode.Mosaic -> CameraStreamRequest(
        camera = this,
        subtype = MOSAIC_SUBTYPE,
        mode = PlayerMode.Mosaic,
        audioMode = AudioMode.Disabled,
    )

    PlayerMode.Fullscreen -> CameraStreamRequest(
        camera = this,
        subtype = FULLSCREEN_SUBTYPE,
        mode = PlayerMode.Fullscreen,
        audioMode = AudioMode.Enabled,
    )
}
