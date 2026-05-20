package com.sentinela.camtv.ui.cameras

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.rtspCameraDraftDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "rtsp_camera_draft",
)

data class RtspCameraDraft(
    val name: String = "",
    val mainUrl: String = "",
    val subUrl: String = "",
)

interface RtspCameraDraftRepository {
    fun observeDraft(): Flow<RtspCameraDraft>
    suspend fun saveDraft(draft: RtspCameraDraft)
}

class DataStoreRtspCameraDraftRepository(
    private val dataStore: DataStore<Preferences>,
) : RtspCameraDraftRepository {
    override fun observeDraft(): Flow<RtspCameraDraft> = dataStore.data.map { preferences ->
        RtspCameraDraft(
            name = preferences[RTSP_NAME].orEmpty(),
            mainUrl = preferences[RTSP_MAIN_URL].orEmpty(),
            subUrl = preferences[RTSP_SUB_URL].orEmpty(),
        )
    }

    override suspend fun saveDraft(draft: RtspCameraDraft) {
        dataStore.edit { preferences ->
            preferences[RTSP_NAME] = draft.name
            preferences[RTSP_MAIN_URL] = draft.mainUrl
            preferences[RTSP_SUB_URL] = draft.subUrl
        }
    }

    private companion object {
        val RTSP_NAME = stringPreferencesKey("rtsp_name")
        val RTSP_MAIN_URL = stringPreferencesKey("rtsp_main_url")
        val RTSP_SUB_URL = stringPreferencesKey("rtsp_sub_url")
    }
}

fun rtspCameraDraftRepository(context: Context): RtspCameraDraftRepository =
    DataStoreRtspCameraDraftRepository(context.applicationContext.rtspCameraDraftDataStore)
