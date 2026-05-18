package com.sentinela.camtv.data.onvif

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnvifEndpointNormalizerTest {
    @Test
    fun keepsFullHttpUrl() {
        assertEquals(
            "http://192.168.1.50/custom/onvif",
            OnvifEndpointNormalizer.normalize(" http://192.168.1.50/custom/onvif "),
        )
    }

    @Test
    fun convertsHostToDefaultDeviceServiceUrl() {
        assertEquals(
            "http://192.168.1.50/onvif/device_service",
            OnvifEndpointNormalizer.normalize("192.168.1.50"),
        )
    }

    @Test
    fun ignoresBlankInput() {
        assertNull(OnvifEndpointNormalizer.normalize("   "))
    }
}
