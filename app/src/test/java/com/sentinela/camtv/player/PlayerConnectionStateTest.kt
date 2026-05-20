package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerConnectionStateTest {
    @Test
    fun authErrorHasCredentialMessageAndIsTerminal() {
        val state = PlayerConnectionState.AuthenticationFailed

        assertEquals("Erro: login ou senha inválidos", state.statusText())
        assertTrue(state.isTerminalError())
    }

    @Test
    fun connectionRefusedHasSpecificMessageAndIsTerminal() {
        val state = PlayerConnectionState.ConnectionRefused

        assertEquals("Erro: conexão recusada", state.statusText())
        assertTrue(state.isTerminalError())
    }

    @Test
    fun udpLikelyBlockedSuggestsStabilityMode() {
        assertEquals(
            "Aviso: UDP instável, tente modo estabilidade",
            PlayerConnectionState.UdpLikelyBlocked.statusText(),
        )
    }
}
