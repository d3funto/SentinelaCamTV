package com.sentinela.camtv.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CameraEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SentinelaDatabase : RoomDatabase() {
    abstract fun cameraDao(): CameraDao
}
