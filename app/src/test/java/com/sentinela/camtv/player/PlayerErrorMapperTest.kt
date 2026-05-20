package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerErrorMapperTest {
    @Test
    fun refusedRtspPortMapsToConnectionRefused() {
        assertEquals(
            PlayerConnectionState.ConnectionRefused,
            PlayerErrorMapper.mapDiagnosticsText(
                details = "ConnectException: connect failed: ECONNREFUSED (Connection refused)",
                transportMode = RtspTransportMode.TcpOnly,
            ),
        )
    }

    @Test
    fun unreachableHostStillMapsToNetworkOffline() {
        assertEquals(
            PlayerConnectionState.NetworkOffline,
            PlayerErrorMapper.mapDiagnosticsText(
                details = "UnknownHostException: host not found",
                transportMode = RtspTransportMode.TcpOnly,
            ),
        )
    }
}
