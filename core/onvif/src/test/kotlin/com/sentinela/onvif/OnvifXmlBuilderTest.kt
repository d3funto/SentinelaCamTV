package com.sentinela.onvif

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnvifXmlBuilderTest {
    @Test
    fun probeContainsNetworkVideoTransmitterType() {
        val xml = OnvifXmlBuilder.probe(messageId = "uuid:test")

        assertTrue(xml.contains("uuid:test"))
        assertTrue(xml.contains("NetworkVideoTransmitter"))
    }

    @Test
    fun getCapabilitiesRequestsAllCategoriesWithoutCredentials() {
        val xml = OnvifXmlBuilder.getCapabilities()

        assertTrue(xml.contains("GetCapabilities"))
        assertTrue(xml.contains("All"))
        assertFalse(xml.contains("real-device-secret"))
    }

    @Test
    fun getStreamUriUsesRtspTransportAndProfileToken() {
        val xml = OnvifXmlBuilder.getStreamUri("Profile_1")

        assertTrue(xml.contains("Profile_1"))
        assertTrue(xml.contains("RTSP"))
        assertFalse(xml.contains("password"))
    }
}
