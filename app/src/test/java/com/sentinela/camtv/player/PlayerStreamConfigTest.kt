package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerStreamConfigTest {
    @Test
    fun defaultStreamConfigStartsWithUdpFirstRtspTransport() {
        val config = defaultPlayerStreamConfig(
            mode = PlayerMode.Mosaic,
            audioMode = AudioMode.Disabled,
        )

        assertEquals(RtspTransportMode.UdpFirst, config.transportMode)
    }

    @Test
    fun defaultStreamConfigKeepsExplicitTcpTransport() {
        val config = defaultPlayerStreamConfig(
            mode = PlayerMode.Fullscreen,
            audioMode = AudioMode.Enabled,
            transportMode = RtspTransportMode.TcpOnly,
        )

        assertEquals(RtspTransportMode.TcpOnly, config.transportMode)
    }

    @Test
    fun lowerLatencyModeUsesTinyBuffersAndUdpPreference() {
        val config = defaultPlayerStreamConfig(
            mode = PlayerMode.Mosaic,
            audioMode = AudioMode.Disabled,
            transmissionMode = TransmissionMode.MENOR_LATENCIA,
        )

        assertEquals(RtspTransportMode.UdpFirst, config.transportMode)
        assertEquals(100, config.bufferPreset.minBufferMs)
        assertEquals(200, config.bufferPreset.maxBufferMs)
    }

    @Test
    fun qualityModeUsesTcpAndLargerBuffers() {
        val config = defaultPlayerStreamConfig(
            mode = PlayerMode.Fullscreen,
            audioMode = AudioMode.Enabled,
            transmissionMode = TransmissionMode.QUALIDADE,
        )

        assertEquals(RtspTransportMode.TcpOnly, config.transportMode)
        assertEquals(1_000, config.bufferPreset.minBufferMs)
        assertEquals(5_000, config.bufferPreset.maxBufferMs)
    }
}
