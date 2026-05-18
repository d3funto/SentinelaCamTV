package com.sentinela.camtv.player

import com.sentinela.camtv.config.DvrConnectionConfig
import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.domain.IntelbrasDvrChannel
import org.junit.Assert.assertEquals
import org.junit.Test

class IntelbrasRtspUrlBuilderTest {
    @Test
    fun buildsMhdxRtspUrlFromProvidedDvrConfig() {
        val config = DvrConnectionConfig(
            host = "203.0.113.10",
            username = "viewer",
            password = "test-pass",
            rtspPort = 8554,
        )
        val source = IntelbrasDvrChannel(
            channel = 5,
            subtype = 1,
        )

        val url = IntelbrasRtspUrlBuilder(config).build(source)

        assertEquals(
            "rtsp://viewer:test-pass@203.0.113.10:8554/cam/realmonitor" +
                "?channel=5&subtype=1&unicast=true&proto=Onvif",
            url,
        )
    }

    @Test
    fun encodesCredentialsBeforeBuildingRtspUrl() {
        val config = DvrConnectionConfig(
            host = "203.0.113.20",
            username = "user name",
            password = "pass@word",
            rtspPort = 554,
        )

        val url = buildIntelbrasRtspUrl(
            dvrConfig = config,
            channel = 1,
            subtype = 0,
        )

        assertEquals(
            "rtsp://user%20name:pass%40word@203.0.113.20:554/cam/realmonitor" +
                "?channel=1&subtype=0&unicast=true&proto=Onvif",
            url,
        )
    }

    @Test
    fun buildsUrlFromCameraStreamRequestSubtype() {
        val config = DvrConnectionConfig(
            host = "203.0.113.30",
            username = "viewer",
            password = "test-pass",
            rtspPort = 554,
        )
        val camera = Camera(
            id = "cam-5",
            name = "CAM5",
            source = IntelbrasDvrChannel(
                channel = 5,
                subtype = 1,
            ),
        )
        val request = camera.streamRequestFor(PlayerMode.Fullscreen)

        val url = IntelbrasRtspUrlBuilder(config).build(request)

        assertEquals(
            "rtsp://viewer:test-pass@203.0.113.30:554/cam/realmonitor" +
                "?channel=5&subtype=0&unicast=true&proto=Onvif",
            url,
        )
    }
}
