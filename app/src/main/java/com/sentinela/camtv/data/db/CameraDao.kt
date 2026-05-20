package com.sentinela.camtv.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CameraDao {
    @Query("SELECT * FROM cameras ORDER BY position ASC")
    fun observeAll(): Flow<List<CameraEntity>>

    @Query("SELECT * FROM cameras WHERE enabled = 1 ORDER BY position ASC")
    fun observeEnabled(): Flow<List<CameraEntity>>

    @Query("SELECT COUNT(*) FROM cameras WHERE enabled = 1")
    suspend fun enabledCount(): Int

    @Query("SELECT * FROM cameras WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CameraEntity?

    @Query("SELECT id FROM cameras ORDER BY position ASC")
    suspend fun orderedIds(): List<String>

    @Upsert
    suspend fun upsert(camera: CameraEntity)

    @Upsert
    suspend fun upsertAll(cameras: List<CameraEntity>)

    @Query("UPDATE cameras SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Int)

    @Query("UPDATE cameras SET authFailure = :authFailure WHERE id = :id")
    suspend fun updateAuthFailure(id: String, authFailure: Boolean)

    @Query("DELETE FROM cameras WHERE id = :id")
    suspend fun deleteById(id: String)
}
