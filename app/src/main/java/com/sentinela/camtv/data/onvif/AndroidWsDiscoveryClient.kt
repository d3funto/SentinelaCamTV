package com.sentinela.camtv.data.onvif

import android.content.Context
import android.net.wifi.WifiManager
import com.sentinela.onvif.DiscoveredOnvifDevice
import com.sentinela.onvif.UdpWsDiscoveryClient
import com.sentinela.onvif.WsDiscoveryClient

class AndroidWsDiscoveryClient(
    context: Context,
    private val delegate: UdpWsDiscoveryClient = UdpWsDiscoveryClient(),
) : WsDiscoveryClient {
    private val appContext = context.applicationContext

    override fun discover(timeoutMillis: Int): List<DiscoveredOnvifDevice> {
        val wifiManager = appContext.getSystemService(WifiManager::class.java)
        val multicastLock = wifiManager?.createMulticastLock("sentinela-onvif-discovery")?.apply {
            setReferenceCounted(false)
        }

        return try {
            multicastLock?.acquire()
            delegate.discover(timeoutMillis)
        } finally {
            if (multicastLock?.isHeld == true) {
                multicastLock.release()
            }
        }
    }
}
