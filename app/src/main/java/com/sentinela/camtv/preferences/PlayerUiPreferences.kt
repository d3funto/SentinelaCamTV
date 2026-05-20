package com.sentinela.camtv.preferences

import com.sentinela.camtv.player.TransmissionMode

data class PlayerUiPreferences(
    val showPlayerInfo: Boolean = true,
    val showMosaicInfo: Boolean = true,
    val showFullscreenInfo: Boolean = true,
    val globalTransmissionMode: TransmissionMode = TransmissionMode.MENOR_LATENCIA,
)
