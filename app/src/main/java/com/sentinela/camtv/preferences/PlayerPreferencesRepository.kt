package com.sentinela.camtv.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sentinela.camtv.player.TransmissionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.playerPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "player_preferences",
)

class PlayerPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    val preferences: Flow<PlayerUiPreferences> = dataStore.data.map { preferences ->
        PlayerUiPreferences(
            showPlayerInfo = preferences[SHOW_PLAYER_INFO] ?: true,
            showMosaicInfo = preferences[SHOW_MOSAIC_INFO] ?: preferences[SHOW_PLAYER_INFO] ?: true,
            showFullscreenInfo = preferences[SHOW_FULLSCREEN_INFO] ?: preferences[SHOW_PLAYER_INFO] ?: true,
            globalTransmissionMode = preferences[GLOBAL_TRANSMISSION_MODE]
                ?.let { value -> runCatching { TransmissionMode.valueOf(value) }.getOrNull() }
                ?: TransmissionMode.MENOR_LATENCIA,
        )
    }

    override fun observePreferences(): Flow<PlayerUiPreferences> = preferences

    override suspend fun setShowPlayerInfo(showPlayerInfo: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_PLAYER_INFO] = showPlayerInfo
            preferences[SHOW_MOSAIC_INFO] = showPlayerInfo
            preferences[SHOW_FULLSCREEN_INFO] = showPlayerInfo
        }
    }

    override suspend fun setShowMosaicInfo(showMosaicInfo: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_MOSAIC_INFO] = showMosaicInfo
        }
    }

    override suspend fun setShowFullscreenInfo(showFullscreenInfo: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_FULLSCREEN_INFO] = showFullscreenInfo
        }
    }

    override suspend fun setGlobalTransmissionMode(transmissionMode: TransmissionMode) {
        dataStore.edit { preferences ->
            preferences[GLOBAL_TRANSMISSION_MODE] = transmissionMode.name
        }
    }

    private companion object {
        val SHOW_PLAYER_INFO = booleanPreferencesKey("show_player_info")
        val SHOW_MOSAIC_INFO = booleanPreferencesKey("show_mosaic_info")
        val SHOW_FULLSCREEN_INFO = booleanPreferencesKey("show_fullscreen_info")
        val GLOBAL_TRANSMISSION_MODE = stringPreferencesKey("global_transmission_mode")
    }
}

fun playerPreferencesRepository(context: Context): PlayerPreferencesRepository =
    PlayerPreferencesRepository(context.applicationContext.playerPreferencesDataStore)
