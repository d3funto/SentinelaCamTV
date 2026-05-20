package com.sentinela.camtv.data.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class RtspCredentialResolverTest {
    @Test
    fun separateFieldsOverrideUrlCredentials() {
        val credentials = RtspCredentialResolver.resolve(
            main = RtspUrlSanitizer.sanitize(rtspWithUserInfo()),
            sub = null,
            username = "field-user",
            password = "field-pass",
        )

        assertEquals("field-user", credentials.username)
        assertEquals("field-pass", credentials.password)
    }

    @Test
    fun embeddedUrlCredentialsArePreservedWhenFieldsAreBlank() {
        val credentials = RtspCredentialResolver.resolve(
            main = RtspUrlSanitizer.sanitize(rtspWithUserInfo()),
            sub = null,
            username = "",
            password = "",
        )

        assertEquals("url-user", credentials.username)
        assertEquals("url-pass", credentials.password)
    }

    private fun rtspWithUserInfo(): String =
        "rtsp://" + "url-user:url-pass@" + "198.51.100.10/live"
}
