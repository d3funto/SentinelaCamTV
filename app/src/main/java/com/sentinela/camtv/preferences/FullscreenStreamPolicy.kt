package com.sentinela.camtv.preferences

import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.player.CameraStreamRequest
import com.sentinela.camtv.player.PlayerMode
import com.sentinela.camtv.player.streamRequestFor

fun PlayerUiPreferences.fullscreenStreamRequestFor(camera: Camera): CameraStreamRequest {
    val request = camera.streamRequestFor(PlayerMode.Fullscreen)
    return request.copy(
        audioMode = fullscreenAudioModeFor(camera),
        transmissionMode = globalTransmissionMode,
    )
}
