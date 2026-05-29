package com.sentinela.camtv.ui.mosaic

internal object MosaicAutoQualityPolicy {
    const val DECODER_FAILURES_BEFORE_SD = 2

    fun shouldFallbackToSdAfterDecoderFailures(failureCount: Int): Boolean =
        failureCount >= DECODER_FAILURES_BEFORE_SD
}
