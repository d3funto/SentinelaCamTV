package com.sentinela.camtv.data.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraOrderTest {
    @Test
    fun removalKeepsOnlyTheSelectedCameraOutOfTheOrder() {
        val remaining = CameraOrder.remainingIdsAfterRemoval(
            orderedIds = listOf("onvif-1", "onvif-2", "rtsp-1", "onvif-3"),
            removedId = "rtsp-1",
        )

        assertEquals(listOf("onvif-1", "onvif-2", "onvif-3"), remaining)
    }

    @Test
    fun removalDoesNotAffectOtherCamerasWithRelatedIds() {
        val remaining = CameraOrder.remainingIdsAfterRemoval(
            orderedIds = listOf("dvr-1", "dvr-2", "dvr-3", "dvr-4", "dvr-5"),
            removedId = "dvr-3",
        )

        assertEquals(listOf("dvr-1", "dvr-2", "dvr-4", "dvr-5"), remaining)
    }
}
