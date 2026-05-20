package com.sentinela.camtv.data.onvif

import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URI

object OnvifEndpointPolicy {
    fun requireAllowedEndpoint(endpoint: String): String {
        val uri = runCatching { URI(endpoint) }.getOrNull()
            ?: throw IOException("Endereço ONVIF inválido.")
        val scheme = uri.scheme?.lowercase()
        val host = uri.host

        if (scheme == "https") return endpoint
        if (scheme != "http" || host.isNullOrBlank()) {
            throw IOException("Endereço ONVIF inválido.")
        }
        if (!hostResolvesToLocalNetwork(host)) {
            throw IOException("ONVIF HTTP só é permitido na rede local.")
        }
        return endpoint
    }

    private fun hostResolvesToLocalNetwork(host: String): Boolean =
        runCatching {
            InetAddress.getAllByName(host).any { address ->
                address.isAnyLocalAddress ||
                    address.isLoopbackAddress ||
                    address.isLinkLocalAddress ||
                    address.isSiteLocalAddress ||
                    address.isPrivateIpv4() ||
                    address.isUniqueLocalIpv6()
            }
        }.getOrDefault(false)

    private fun InetAddress.isPrivateIpv4(): Boolean {
        val address = this as? Inet4Address ?: return false
        val bytes = address.address.map { byte -> byte.toInt() and 0xFF }
        return bytes[0] == 10 ||
            (bytes[0] == 172 && bytes[1] in 16..31) ||
            (bytes[0] == 192 && bytes[1] == 168)
    }

    private fun InetAddress.isUniqueLocalIpv6(): Boolean {
        val address = this as? Inet6Address ?: return false
        val firstByte = address.address.first().toInt() and 0xFF
        return firstByte in 0xFC..0xFD
    }
}
