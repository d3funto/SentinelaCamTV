package com.sentinela.camtv.ui.cameras

import com.sentinela.onvif.DiscoveredOnvifDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class OnvifDeviceLabelTest {
    @Test
    fun displayLabelPrefersDeviceNameOverGenericCountryScope() {
        val device = DiscoveredOnvifDevice(
            endpointReference = "urn:uuid:test",
            types = emptyList(),
            xAddrs = listOf("http://192.168.1.50/onvif/device_service"),
            scopes = listOf(
                "onvif://www.onvif.org/location/country/China",
                "onvif://www.onvif.org/name/iC3LOW",
            ),
        )

        assertEquals("iC3LOW", device.displayLabel())
    }

    @Test
    fun displayLabelFallsBackToHardwareBeforeXAddr() {
        val device = DiscoveredOnvifDevice(
            endpointReference = "urn:uuid:test",
            types = emptyList(),
            xAddrs = listOf("http://192.168.1.50/onvif/device_service"),
            scopes = listOf("onvif://www.onvif.org/hardware/MHDX1004"),
        )

        assertEquals("MHDX1004", device.displayLabel())
    }
}
