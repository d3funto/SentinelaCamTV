package com.sentinela.camtv.data.onvif

import com.sentinela.onvif.OnvifMediaProfile
import com.sentinela.onvif.OnvifStreamUri
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

    @Test
    fun groupsFiveDvrChannelsWithMainAndSubStreams() {
        val selections = OnvifProfileSelector.selectCameras(
            (1..5).flatMap { channel ->
                listOf(
                    resolvedProfile(channel = channel, subtype = 0),
                    resolvedProfile(channel = channel, subtype = 1),
                )
            },
        )

        assertEquals(5, selections.size)
        assertEquals((1..5).toList(), selections.map { it.channelNumber })
        assertEquals("channel-1-main", selections.first().main.profile.token)
        assertEquals("channel-1-sub", selections.first().sub?.profile?.token)
    }

    @Test
    fun limitsDvrChannelsToSixteenCameras() {
        val selections = OnvifProfileSelector.selectCameras(
            (1..17).flatMap { channel ->
                listOf(
                    resolvedProfile(channel = channel, subtype = 0),
                    resolvedProfile(channel = channel, subtype = 1),
                )
            },
        )

        assertEquals(16, selections.size)
        assertEquals(16, selections.last().channelNumber)
    }

    @Test
    fun simpleIpCameraWithoutChannelBecomesSingleCamera() {
        val selections = OnvifProfileSelector.selectCameras(
            listOf(
                resolvedProfile(token = "main", name = "mainStream", uri = "rtsp://198.51.100.10/live/main"),
                resolvedProfile(token = "sub", name = "subStream", uri = "rtsp://198.51.100.10/live/sub"),
            ),
        )

        assertEquals(1, selections.size)
        assertNull(selections.single().channelNumber)
        assertEquals("main", selections.single().main.profile.token)
        assertEquals("sub", selections.single().sub?.profile?.token)
    }

    private fun resolvedProfile(
        channel: Int,
        subtype: Int,
    ): ResolvedOnvifProfile =
        resolvedProfile(
            token = "channel-$channel-${if (subtype == 0) "main" else "sub"}",
            name = if (subtype == 0) "mainStream" else "subStream",
            uri = "rtsp://198.51.100.10/cam/realmonitor?channel=$channel&subtype=$subtype",
        )

    private fun resolvedProfile(
        token: String,
        name: String,
        uri: String,
    ): ResolvedOnvifProfile =
        ResolvedOnvifProfile(
            profile = OnvifMediaProfile(
                token = token,
                name = name,
                fixed = true,
            ),
            streamUri = OnvifStreamUri(
                uri = uri,
                invalidAfterConnect = false,
                invalidAfterReboot = false,
                timeout = null,
            ),
        )
}
