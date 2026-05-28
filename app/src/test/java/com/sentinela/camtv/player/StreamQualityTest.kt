package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamQualityTest {
    @Test
    fun sdUsesSubtypeOneAndHdUsesSubtypeZero() {
        assertEquals(1, StreamQuality.SD.subtype)
        assertEquals(0, StreamQuality.HD.subtype)
    }

    @Test
    fun nextTogglesBetweenSdAndHd() {
        assertEquals(StreamQuality.HD, StreamQuality.SD.next())
        assertEquals(StreamQuality.SD, StreamQuality.HD.next())
    }
}
