package com.sentinela.onvif

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class UdpWsDiscoveryClient(
    private val multicastAddress: String = "239.255.255.250",
    private val discoveryPort: Int = 3702,
) : WsDiscoveryClient {
    override fun discover(timeoutMillis: Int): List<DiscoveredOnvifDevice> {
        val request = OnvifXmlBuilder.probe().toByteArray()
        val target = InetAddress.getByName(multicastAddress)
        val responseBuffer = ByteArray(64 * 1024)
        val devices = linkedMapOf<String, DiscoveredOnvifDevice>()

        DatagramSocket().use { socket ->
            socket.soTimeout = timeoutMillis
            socket.broadcast = true
            socket.send(DatagramPacket(request, request.size, target, discoveryPort))

            val startedAt = System.currentTimeMillis()
            while (System.currentTimeMillis() - startedAt < timeoutMillis) {
                val packet = DatagramPacket(responseBuffer, responseBuffer.size)
                try {
                    socket.receive(packet)
                } catch (_: SocketTimeoutException) {
                    break
                }

                val xml = String(packet.data, packet.offset, packet.length)
                OnvifXmlParser.parseProbeMatches(xml).forEach { device ->
                    val key = device.endpointReference.ifBlank {
                        device.xAddrs.firstOrNull().orEmpty()
                    }
                    if (key.isNotBlank()) {
                        devices[key] = device
                    }
                }
            }
        }

        return devices.values.toList()
    }
}
