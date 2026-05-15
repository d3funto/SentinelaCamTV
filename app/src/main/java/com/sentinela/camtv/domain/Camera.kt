package com.sentinela.camtv.domain

data class Camera(
    val id: String,
    val name: String,
    val source: CameraSource,
)

sealed interface CameraSource

data class IntelbrasDvrChannel(
    val channel: Int,
    val subtype: Int,
) : CameraSource
