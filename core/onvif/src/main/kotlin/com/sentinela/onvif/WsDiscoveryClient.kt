package com.sentinela.onvif

interface WsDiscoveryClient {
    fun discover(timeoutMillis: Int = 3_000): List<DiscoveredOnvifDevice>
}
