package com.sentinela.camtv.data.onvif

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class OnvifEndpointPolicyTest {
    @Test
    fun allowsCleartextHttpForPrivateIpv4() {
        val endpoint = "http://192.168.1.31/onvif/device_service"

        assertEquals(endpoint, OnvifEndpointPolicy.requireAllowedEndpoint(endpoint))
    }

    @Test
    fun blocksCleartextHttpForPublicIpv4() {
        val error = assertThrows(IOException::class.java) {
            OnvifEndpointPolicy.requireAllowedEndpoint("http://203.0.113.10/onvif/device_service")
        }

        assertEquals("ONVIF HTTP só é permitido na rede local.", error.message)
    }

    @Test
    fun allowsHttpsOutsideLocalNetwork() {
        val endpoint = "https://example.com/onvif/device_service"

        assertEquals(endpoint, OnvifEndpointPolicy.requireAllowedEndpoint(endpoint))
    }

    @Test
    fun rejectsInvalidEndpoint() {
        val error = assertThrows(IOException::class.java) {
            OnvifEndpointPolicy.requireAllowedEndpoint("192.168.1.31/onvif/device_service")
        }

        assertEquals("Endereço ONVIF inválido.", error.message)
    }
}
