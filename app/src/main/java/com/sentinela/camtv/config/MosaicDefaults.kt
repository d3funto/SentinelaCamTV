package com.sentinela.camtv.config

import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.domain.IntelbrasDvrChannel

const val MOSAIC_SUBTYPE = 1

const val SHOW_PLAYER_DEBUG_INFO = true
const val SHOW_CAMERA_LABELS = true
const val SHOW_APP_HEADER = false

const val APP_PADDING_DP = 16
const val TILE_GAP_DP = 8

fun defaultMosaicCameras(): List<Camera> = (1..5).map { channel ->
    Camera(
        id = "cam-$channel",
        name = "CAM$channel",
        source = IntelbrasDvrChannel(
            channel = channel,
            subtype = MOSAIC_SUBTYPE,
        ),
    )
}
