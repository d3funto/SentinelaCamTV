package com.sentinela.camtv.ui.labels

import com.sentinela.camtv.player.AudioMode
import com.sentinela.camtv.player.TransmissionMode
import com.sentinela.camtv.ui.player.StreamQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class UiLabelsTest {
    @Test
    fun audioLabelsUseActivationPattern() {
        assertEquals("Áudio: Ativado", audioLabel(AudioMode.Enabled))
        assertEquals("Áudio: Desativado", audioLabel(AudioMode.Disabled))
    }

    @Test
    fun informationLabelsUseActivationPattern() {
        assertEquals("Ativadas", activationLabel(true))
        assertEquals("Desativadas", activationLabel(false))
    }

    @Test
    fun statusLabelsUseActivationPattern() {
        assertEquals("Ativado", statusLabel(true))
        assertEquals("Desativado", statusLabel(false))
    }

    @Test
    fun transmissionModeLabelsAreUserFacingPortuguese() {
        assertEquals("Menor latência", transmissionModeLabel(TransmissionMode.MENOR_LATENCIA))
        assertEquals("Qualidade", transmissionModeLabel(TransmissionMode.QUALIDADE))
        assertEquals("Modo: Menor latência", transmissionModeMenuLabel(TransmissionMode.MENOR_LATENCIA))
    }

    @Test
    fun streamQualityLabelsUseHdAndSd() {
        assertEquals("Qualidade: HD", streamQualityLabel(StreamQuality.HD))
        assertEquals("Qualidade: SD", streamQualityLabel(StreamQuality.SD))
    }
}
