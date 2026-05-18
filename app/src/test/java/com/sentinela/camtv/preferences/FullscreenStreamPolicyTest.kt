package com.sentinela.camtv.preferences

import com.sentinela.camtv.config.FULLSCREEN_SUBTYPE
import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.domain.IntelbrasDvrChannel
import com.sentinela.camtv.player.AudioMode
import com.sentinela.camtv.player.TransmissionMode
import org.junit.Assert.assertEquals
import org.junit.Test

class FullscreenStreamPolicyTest {
    @Test
    fun fullscreenUsesMainStreamByDefault() {
        val request = PlayerUiPreferences().fullscreenStreamRequestFor(camera(id = "cam-5"))

        assertEquals(FULLSCREEN_SUBTYPE, request.subtype)
    }

    @Test
    fun fullscreenRequestUsesGlobalAudioAndTransmissionMode() {
        val request = PlayerUiPreferences(
            fullscreenAudioEnabled = false,
            globalTransmissionMode = TransmissionMode.QUALIDADE,
        ).fullscreenStreamRequestFor(camera(id = "cam-5"))

        assertEquals(FULLSCREEN_SUBTYPE, request.subtype)
        assertEquals(AudioMode.Disabled, request.audioMode)
        assertEquals(TransmissionMode.QUALIDADE, request.transmissionMode)
    }

    private fun camera(id: String): Camera = Camera(
        id = id,
        name = id.uppercase(),
        source = IntelbrasDvrChannel(
            channel = 1,
            subtype = FULLSCREEN_SUBTYPE,
        ),
    )
}
