package com.sentinela.camtv.ui.mosaic

import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.domain.RtspCameraSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MosaicBackNavigationTest {
    @Test
    fun emptyLoadedMosaicReturnsHomeOnBack() {
        val state = MosaicUiState(
            cameras = emptyList(),
            isLoading = false,
        )

        assertTrue(shouldReturnHomeOnMosaicBack(state))
    }

    @Test
    fun loadingMosaicDoesNotReturnHomeOnBack() {
        val state = MosaicUiState(
            cameras = emptyList(),
            isLoading = true,
        )

        assertFalse(shouldReturnHomeOnMosaicBack(state))
    }

    @Test
    fun mosaicWithCamerasKeepsQuickMenuBehaviorOnBack() {
        val state = MosaicUiState(
            cameras = listOf(testCamera()),
            isLoading = false,
        )

        assertFalse(shouldReturnHomeOnMosaicBack(state))
    }

    private fun testCamera(): Camera = Camera(
        id = "camera-1",
        name = "CAM1",
        source = RtspCameraSource(
            mainRtspUrl = "rtsp://192.0.2.10:554/cam1",
            subRtspUrl = null,
        ),
    )
}
