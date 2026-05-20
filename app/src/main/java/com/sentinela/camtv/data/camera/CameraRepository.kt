package com.sentinela.camtv.data.camera

import com.sentinela.camtv.domain.Camera
import kotlinx.coroutines.flow.Flow

interface CameraRepository {
    fun observeAllCameras(): Flow<List<Camera>>
    fun observeEnabledCameras(): Flow<List<Camera>>
    suspend fun hasEnabledCameras(): Boolean
    suspend fun seedDebugCamerasIfEmpty(cameras: List<Camera>)
    suspend fun saveManualRtspCamera(
        id: String,
        name: String,
        rtspUrl: String,
        subRtspUrl: String?,
        username: String?,
        password: String?,
        position: Int,
    )
    suspend fun saveOnvifCamera(
        id: String,
        name: String,
        endpoint: String,
        onvifDeviceServiceUrl: String,
        mainRtspUrl: String,
        subRtspUrl: String?,
        username: String?,
        password: String?,
        position: Int,
    )
    suspend fun updateCameraOrder(cameraIds: List<String>)
    suspend fun setAuthenticationFailure(cameraId: String, hasFailure: Boolean)
    suspend fun deleteCamera(cameraId: String)
}
