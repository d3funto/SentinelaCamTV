package com.sentinela.camtv.ui.mosaic

import com.sentinela.camtv.player.StreamQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MosaicQualityOverrideTest {
    @Test
    fun cameraOverrideFallsBackToSdWhenGlobalQualityIsHd() {
        val state = MosaicUiState(
            streamQuality = StreamQuality.HD,
            autoQualityOverrides = mapOf("cam-1" to StreamQuality.SD),
        )

        assertEquals(StreamQuality.SD, state.effectiveStreamQuality("cam-1"))
        assertTrue(state.isAutoQualityDowngraded("cam-1"))
    }

    @Test
    fun cameraWithoutOverrideUsesGlobalQuality() {
        val state = MosaicUiState(
            streamQuality = StreamQuality.HD,
            autoQualityOverrides = mapOf("cam-1" to StreamQuality.SD),
        )

        assertEquals(StreamQuality.HD, state.effectiveStreamQuality("cam-2"))
        assertFalse(state.isAutoQualityDowngraded("cam-2"))
    }

    @Test
    fun sdGlobalQualityDoesNotMarkOverrideAsAutoDowngrade() {
        val state = MosaicUiState(
            streamQuality = StreamQuality.SD,
            autoQualityOverrides = mapOf("cam-1" to StreamQuality.SD),
        )

        assertEquals(StreamQuality.SD, state.effectiveStreamQuality("cam-1"))
        assertFalse(state.isAutoQualityDowngraded("cam-1"))
    }

    @Test
    fun firstDecoderFailureDoesNotFallbackToSd() {
        assertFalse(MosaicAutoQualityPolicy.shouldFallbackToSdAfterDecoderFailures(1))
    }

    @Test
    fun repeatedDecoderFailuresFallbackToSd() {
        assertTrue(
            MosaicAutoQualityPolicy.shouldFallbackToSdAfterDecoderFailures(
                MosaicAutoQualityPolicy.DECODER_FAILURES_BEFORE_SD,
            ),
        )
    }
}
