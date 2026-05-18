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

    @Test
    fun authenticatedRequestsUseUsernameTokenWithoutPlainPassword() {
        val token = OnvifUsernameToken(
            username = "test-user",
            passwordDigest = "digest-value",
            nonceBase64 = "nonce-value",
            created = "2026-05-18T10:00:00Z",
        )

        val xml = OnvifXmlBuilder.getProfiles(token)

        assertTrue(xml.contains("UsernameToken"))
        assertTrue(xml.contains("test-user"))
        assertTrue(xml.contains("digest-value"))
        assertTrue(xml.contains("nonce-value"))
        assertTrue(xml.contains("2026-05-18T10:00:00Z"))
        assertFalse(xml.contains("test-password"))
    }
}
