package com.sentinela.camtv.player

enum class PlayerMode {
    Mosaic,
    Fullscreen,
}

enum class AudioMode {
    Disabled,
    Enabled,
}

enum class RtspTransportMode {
    UdpFirst,
    TcpOnly,
}

enum class TransmissionMode {
    MENOR_LATENCIA,
    QUALIDADE,
}

data class PlayerBufferPreset(
    val minBufferMs: Int,
    val maxBufferMs: Int,
    val bufferForPlaybackMs: Int,
    val bufferAfterRebufferMs: Int,
)

object PlayerBufferPresets {
    val LowLatency = PlayerBufferPreset(
        minBufferMs = 100,
        maxBufferMs = 200,
        bufferForPlaybackMs = 100,
        bufferAfterRebufferMs = 100,
    )

    val Quality = PlayerBufferPreset(
        minBufferMs = 1_000,
        maxBufferMs = 5_000,
        bufferForPlaybackMs = 500,
        bufferAfterRebufferMs = 1_000,
    )
}

data class PlayerStreamConfig(
    val mode: PlayerMode,
    val audioMode: AudioMode,
    val bufferPreset: PlayerBufferPreset,
    val rtspTimeoutMs: Long,
    val transportMode: RtspTransportMode,
    val transmissionMode: TransmissionMode,
    val enableDecoderFallback: Boolean = false,
)

fun defaultPlayerStreamConfig(
    mode: PlayerMode,
    audioMode: AudioMode,
    transmissionMode: TransmissionMode = TransmissionMode.MENOR_LATENCIA,
    transportMode: RtspTransportMode = transmissionMode.defaultTransportMode(),
    enableDecoderFallback: Boolean = false,
): PlayerStreamConfig = PlayerStreamConfig(
    mode = mode,
    audioMode = audioMode,
    bufferPreset = transmissionMode.bufferPreset(),
    rtspTimeoutMs = transmissionMode.rtspTimeoutMs(),
    transportMode = transportMode,
    transmissionMode = transmissionMode,
    enableDecoderFallback = enableDecoderFallback,
)

fun TransmissionMode.defaultTransportMode(): RtspTransportMode = when (this) {
    TransmissionMode.MENOR_LATENCIA -> RtspTransportMode.UdpFirst
    TransmissionMode.QUALIDADE -> RtspTransportMode.TcpOnly
}

fun TransmissionMode.bufferPreset(): PlayerBufferPreset = when (this) {
    TransmissionMode.MENOR_LATENCIA -> PlayerBufferPresets.LowLatency
    TransmissionMode.QUALIDADE -> PlayerBufferPresets.Quality
}

fun TransmissionMode.rtspTimeoutMs(): Long = when (this) {
    TransmissionMode.MENOR_LATENCIA -> 1_500L
    TransmissionMode.QUALIDADE -> 5_000L
}

fun TransmissionMode.next(): TransmissionMode = when (this) {
    TransmissionMode.MENOR_LATENCIA -> TransmissionMode.QUALIDADE
    TransmissionMode.QUALIDADE -> TransmissionMode.MENOR_LATENCIA
}
