package com.sentinela.camtv.data.onvif

import com.sentinela.onvif.DiscoveredOnvifDevice
import com.sentinela.onvif.OnvifCapabilities
import com.sentinela.onvif.OnvifCredentials
import com.sentinela.onvif.OnvifMediaProfile
import com.sentinela.onvif.OnvifSoapClient
import com.sentinela.onvif.OnvifStreamUri
import com.sentinela.onvif.WsDiscoveryClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultOnvifRepository(
    private val wsDiscoveryClient: WsDiscoveryClient,
    private val soapClient: OnvifSoapClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : OnvifRepository {
    override suspend fun discover(): Result<List<DiscoveredOnvifDevice>> =
        withContext(ioDispatcher) {
            runCatching { wsDiscoveryClient.discover() }
        }

    override suspend fun getCapabilities(
        deviceServiceUrl: String,
        credentials: OnvifCredentials?,
    ): Result<OnvifCapabilities> =
        withContext(ioDispatcher) {
            runCatching {
                val endpoint = OnvifEndpointPolicy.requireAllowedEndpoint(deviceServiceUrl)
                soapClient.getCapabilities(endpoint, credentials).getOrThrow()
            }
        }

    override suspend fun getProfiles(
        mediaServiceUrl: String,
        credentials: OnvifCredentials?,
    ): Result<List<OnvifMediaProfile>> =
        withContext(ioDispatcher) {
            runCatching {
                val endpoint = OnvifEndpointPolicy.requireAllowedEndpoint(mediaServiceUrl)
                soapClient.getProfiles(endpoint, credentials).getOrThrow()
            }
        }

    override suspend fun getStreamUri(
        mediaServiceUrl: String,
        profileToken: String,
        credentials: OnvifCredentials?,
    ): Result<OnvifStreamUri> =
        withContext(ioDispatcher) {
            runCatching {
                val endpoint = OnvifEndpointPolicy.requireAllowedEndpoint(mediaServiceUrl)
                soapClient.getStreamUri(endpoint, profileToken, credentials).getOrThrow()
            }
        }
}
