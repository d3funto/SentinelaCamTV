package com.sentinela.onvif

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class OnvifUsernameTokenFactoryTest {
    @Test
    fun createsDeterministicPasswordDigestToken() {
        val factory = OnvifUsernameTokenFactory(
            nowMillis = { 1_768_579_200_000L },
            nonceBytes = { byteArrayOf(0, 1, 2, 3) },
        )

        val token = factory.create(
            OnvifCredentials(
                username = "test-user",
                password = "test-password",
            ),
        )

        assertEquals("test-user", token.username)
        assertEquals("AAECAw==", token.nonceBase64)
        assertEquals("2026-01-16T16:00:00Z", token.created)
        assertEquals("BoIezg/HaO0eolf0w6iityMWkts=", token.passwordDigest)
        assertFalse(token.passwordDigest.contains("test-password"))
    }

    @Test
    fun base64EncoderHandlesPadding() {
        assertEquals("AQ==", byteArrayOf(1).toBase64())
        assertEquals("AQI=", byteArrayOf(1, 2).toBase64())
        assertEquals("AQID", byteArrayOf(1, 2, 3).toBase64())
    }
}
