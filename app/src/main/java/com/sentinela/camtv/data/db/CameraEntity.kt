package com.sentinela.camtv.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cameras")
data class CameraEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sourceType: String,
    val endpoint: String,
    val onvifDeviceServiceUrl: String?,
    val mainRtspUrl: String?,
    val subRtspUrl: String?,
    val intelbrasChannel: Int?,
    val usernameCipherText: String?,
    val passwordCipherText: String?,
    val position: Int,
    val enabled: Boolean,
    val authFailure: Boolean,
)
