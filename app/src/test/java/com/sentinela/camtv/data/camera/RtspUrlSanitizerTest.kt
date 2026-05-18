package com.sentinela.camtv.data.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RtspUrlSanitizerTest {
    @Test
    fun sanitizeMovesUserInfoOutOfRtspUrl() {
        val sanitized = RtspUrlSanitizer.sanitize(
            "rtsp://admin:secret@198.51.100.10:554/cam/realmonitor?channel=1&subtype=0",
        )

        assertEquals("rtsp://198.51.100.10:554/cam/realmonitor?channel=1&subtype=0", sanitized.urlWithoutUserInfo)
        assertEquals("admin", sanitized.username)
        assertEquals("secret", sanitized.password)
    }

    @Test
    fun sanitizeKeepsCredentialFieldsEmptyWhenUrlHasNoUserInfo() {
        val sanitized = RtspUrlSanitizer.sanitize("rtsp://198.51.100.10/live")

        assertEquals("rtsp://198.51.100.10/live", sanitized.urlWithoutUserInfo)
        assertNull(sanitized.username)
        assertNull(sanitized.password)
    }
}
