package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RtspConnectionTestResultTest {
    @Test
    fun successHasNoUserMessage() {
        assertNull(RtspConnectionTestResult.Success.userMessage("Fluxo principal"))
    }

    @Test
    fun failureUsesTranslatedPlayerState() {
        val result = RtspConnectionTestResult.Failure(PlayerConnectionState.ConnectionRefused)

        assertEquals(
            "Fluxo principal: Erro: conexão recusada",
            result.userMessage("Fluxo principal"),
        )
    }

    @Test
    fun authFailureKeepsCredentialMessage() {
        val result = RtspConnectionTestResult.Failure(PlayerConnectionState.AuthenticationFailed)

        assertEquals(
            "Fluxo secundário: Erro: login ou senha inválidos",
            result.userMessage("Fluxo secundário"),
        )
    }

    @Test
    fun timeoutUsesTranslatedMessage() {
        val result = RtspConnectionTestResult.Failure(PlayerConnectionState.Timeout)

        assertEquals(
            "Fluxo principal: Erro: tempo esgotado",
            result.userMessage("Fluxo principal"),
        )
    }
}
