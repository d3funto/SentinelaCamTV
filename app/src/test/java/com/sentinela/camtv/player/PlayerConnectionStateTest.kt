package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerConnectionStateTest {
    @Test
    fun authErrorHasCredentialMessageAndIsTerminal() {
        val state = PlayerConnectionState.AuthenticationFailed

        assertEquals("Erro: login ou senha invalidos", state.statusText())
        assertTrue(state.isTerminalError())
    }

    @Test
    fun udpLikelyBlockedSuggestsQualityMode() {
        assertEquals(
            "Aviso: UDP instavel, tente modo QUALIDADE",
            PlayerConnectionState.UdpLikelyBlocked.statusText(),
        )
    }
}
