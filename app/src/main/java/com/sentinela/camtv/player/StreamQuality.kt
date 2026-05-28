package com.sentinela.camtv.player

enum class StreamQuality(
    val subtype: Int,
) {
    HD(subtype = 0),
    SD(subtype = 1),
}

fun StreamQuality.next(): StreamQuality = when (this) {
    StreamQuality.HD -> StreamQuality.SD
    StreamQuality.SD -> StreamQuality.HD
}
