package com.sentinela.camtv.preferences

import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.player.AudioMode

@Suppress("UNUSED_PARAMETER")
fun PlayerUiPreferences.fullscreenAudioModeFor(camera: Camera): AudioMode =
    AudioMode.Enabled
