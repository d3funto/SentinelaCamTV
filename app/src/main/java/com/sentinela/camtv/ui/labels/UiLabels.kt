package com.sentinela.camtv.ui.labels

import com.sentinela.camtv.player.AudioMode
import com.sentinela.camtv.player.TransmissionMode
import com.sentinela.camtv.ui.player.StreamQuality

fun activationLabel(active: Boolean): String =
    if (active) "Ativadas" else "Desativadas"

fun statusLabel(active: Boolean): String =
    if (active) "Ativado" else "Desativado"

fun audioLabel(audioMode: AudioMode): String = when (audioMode) {
    AudioMode.Enabled -> "Áudio: Ativado"
    AudioMode.Disabled -> "Áudio: Desativado"
}

fun streamQualityLabel(streamQuality: StreamQuality): String = when (streamQuality) {
    StreamQuality.HD -> "Qualidade: HD"
    StreamQuality.SD -> "Qualidade: SD"
}

fun transmissionModeLabel(transmissionMode: TransmissionMode): String = when (transmissionMode) {
    TransmissionMode.MENOR_LATENCIA -> "Menor latência"
    TransmissionMode.QUALIDADE -> "Qualidade"
}

fun transmissionModeMenuLabel(transmissionMode: TransmissionMode): String =
    "Modo: ${transmissionModeLabel(transmissionMode)}"
