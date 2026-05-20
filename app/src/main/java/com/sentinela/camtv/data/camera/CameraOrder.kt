package com.sentinela.camtv.data.camera

object CameraOrder {
    fun remainingIdsAfterRemoval(
        orderedIds: List<String>,
        removedId: String,
    ): List<String> = orderedIds.filterNot { it == removedId }
}
