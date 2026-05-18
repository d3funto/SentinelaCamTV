package com.sentinela.camtv.data.onvif

import com.sentinela.onvif.OnvifMediaProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnvifProfileSelectorTest {
    @Test
    fun selectsMainAndSubByNameWhenAvailable() {
        val selection = OnvifProfileSelector.select(
            listOf(
                OnvifMediaProfile(token = "Profile_2", name = "subStream", fixed = true),
                OnvifMediaProfile(token = "Profile_1", name = "mainStream", fixed = true),
            ),
        )

        assertEquals("Profile_1", selection?.main?.token)
        assertEquals("Profile_2", selection?.sub?.token)
    }

    @Test
    fun fallsBackToFirstAndSecondProfiles() {
        val selection = OnvifProfileSelector.select(
            listOf(
                OnvifMediaProfile(token = "A", name = "Camera profile A", fixed = true),
                OnvifMediaProfile(token = "B", name = "Camera profile B", fixed = true),
            ),
        )

        assertEquals("A", selection?.main?.token)
        assertEquals("B", selection?.sub?.token)
    }

    @Test
    fun returnsNullWhenThereAreNoProfiles() {
        assertNull(OnvifProfileSelector.select(emptyList()))
    }
}
