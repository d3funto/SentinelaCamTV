package com.sentinela.camtv.preferences

import com.sentinela.camtv.player.TransmissionMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observePreferences(): Flow<PlayerUiPreferences>
    suspend fun setShowPlayerInfo(showPlayerInfo: Boolean)
    suspend fun setShowMosaicInfo(showMosaicInfo: Boolean)
    suspend fun setShowFullscreenInfo(showFullscreenInfo: Boolean)
    suspend fun setFullscreenQuickMenuHintSeen(seen: Boolean)
    suspend fun setGlobalTransmissionMode(transmissionMode: TransmissionMode)
}
