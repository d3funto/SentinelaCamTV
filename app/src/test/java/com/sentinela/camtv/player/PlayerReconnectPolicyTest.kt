package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayerReconnectPolicyTest {
    @Test
    fun watchdogReconnectsSlowConnectingPlayersAfterTwelveSeconds() {
        assertEquals(
            12_000L,
            PlayerReconnectPolicy.watchdogTimeoutFor(
                mode = PlayerMode.Mosaic,
                state = PlayerConnectionState.Connecting,
            ),
        )
    }

    @Test
    fun watchdogUsesShorterBufferingLimitForFullscreen() {
        assertEquals(
            8_000L,
            PlayerReconnectPolicy.watchdogTimeoutFor(
                mode = PlayerMode.Fullscreen,
                state = PlayerConnectionState.Buffering,
            ),
        )
    }

    @Test
    fun watchdogUsesLongerBufferingLimitForMosaic() {
        assertEquals(
            12_000L,
            PlayerReconnectPolicy.watchdogTimeoutFor(
                mode = PlayerMode.Mosaic,
                state = PlayerConnectionState.Buffering,
            ),
        )
    }

    @Test
    fun watchdogIgnoresHealthyPlayingState() {
        assertNull(
            PlayerReconnectPolicy.watchdogTimeoutFor(
                mode = PlayerMode.Fullscreen,
                state = PlayerConnectionState.Playing,
            ),
        )
    }

    @Test
    fun transportFallsBackToTcpAfterRepeatedFailures() {
        assertEquals(RtspTransportMode.UdpFirst, PlayerReconnectPolicy.transportModeForFailureCount(0))
        assertEquals(RtspTransportMode.UdpFirst, PlayerReconnectPolicy.transportModeForFailureCount(1))
        assertEquals(RtspTransportMode.TcpOnly, PlayerReconnectPolicy.transportModeForFailureCount(2))
        assertEquals(RtspTransportMode.TcpOnly, PlayerReconnectPolicy.transportModeForFailureCount(99))
    }
}
