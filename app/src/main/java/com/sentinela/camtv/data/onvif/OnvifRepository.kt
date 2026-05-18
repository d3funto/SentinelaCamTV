package com.sentinela.camtv.data.onvif

import com.sentinela.onvif.DiscoveredOnvifDevice
import com.sentinela.onvif.OnvifCapabilities
import com.sentinela.onvif.OnvifCredentials
import com.sentinela.onvif.OnvifMediaProfile
import com.sentinela.onvif.OnvifStreamUri

interface OnvifRepository {
    suspend fun discover(): Result<List<DiscoveredOnvifDevice>>
    suspend fun getCapabilities(
        deviceServiceUrl: String,
        credentials: OnvifCredentials?,
    ): Result<OnvifCapabilities>
    suspend fun getProfiles(
        mediaServiceUrl: String,
        credentials: OnvifCredentials?,
    ): Result<List<OnvifMediaProfile>>
    suspend fun getStreamUri(
        mediaServiceUrl: String,
        profileToken: String,
        credentials: OnvifCredentials?,
    ): Result<OnvifStreamUri>
}
