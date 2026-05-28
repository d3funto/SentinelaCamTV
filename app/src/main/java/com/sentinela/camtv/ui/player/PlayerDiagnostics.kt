package com.sentinela.camtv.ui.player

import com.sentinela.camtv.player.PlayerConnectionState
import com.sentinela.camtv.player.RtspTransportMode
import com.sentinela.camtv.player.StreamQuality
import com.sentinela.camtv.player.TransmissionMode
import com.sentinela.camtv.player.statusText
import com.sentinela.camtv.ui.labels.transmissionModeLabel

internal data class PlayerDiagnostics(
    val cameraName: String,
    val connectionState: PlayerConnectionState,
    val subtype: Int,
    val transmissionMode: TransmissionMode,
    val initialTransportMode: RtspTransportMode,
    val transportMode: RtspTransportMode,
    val decoderFallbackEnabled: Boolean,
    val videoSize: String? = null,
    val framesPerSecond: Float? = null,
    val bandwidthEstimateBps: Long? = null,
    val bufferMs: Long? = null,
    val mimeType: String? = null,
    val codecs: String? = null,
    val decoderName: String? = null,
    val droppedFrames: Int = 0,
    val reconnectAttempt: Int = 0,
    val consecutiveFailures: Int = 0,
    val readyMs: Long? = null,
    val firstFrameMs: Long? = null,
    val renderedFirstFrame: Boolean = false,
    val lastReconnectReason: String? = null,
    val lastWatchdogReason: String? = null,
    val lastError: String? = null,
)

internal object PlayerDiagnosticsFormatter {
    fun overlayLines(diagnostics: PlayerDiagnostics): List<String> = buildList {
        val quality = StreamQuality.entries.firstOrNull { it.subtype == diagnostics.subtype } ?: StreamQuality.SD
        add("${diagnostics.cameraName} • ${quality.name} • subtype ${diagnostics.subtype}")

        val videoLine = listOfNotNull(
            diagnostics.videoSize,
            diagnostics.framesPerSecond?.takeIf { it > 0f }?.let { "${it.clean()} FPS" },
        ).joinToString(" • ")
        if (videoLine.isNotBlank()) add(videoLine)

        add("${diagnostics.connectionState.statusText().removePrefix("Estado: ")} • ${transmissionModeLabel(diagnostics.transmissionMode)}")
        add("RTSP: ${diagnostics.transportLabel()}")

        diagnostics.codecLabel()?.let { add("Codec: $it") }
        add("Decoder: ${diagnostics.decoderLabel()}")

        val networkLine = listOfNotNull(
            diagnostics.bandwidthEstimateBps?.takeIf { it > 0 }?.let { "Banda ${it.kbps()} kb/s" },
            diagnostics.bufferMs?.takeIf { it >= 0 }?.let { "Buffer ${it} ms" },
        ).joinToString(" • ")
        if (networkLine.isNotBlank()) add(networkLine)

        add("Drop ${diagnostics.droppedFrames} • Reconn ${diagnostics.reconnectAttempt} • Falhas ${diagnostics.consecutiveFailures}")

        val timingLine = listOfNotNull(
            diagnostics.readyMs?.let { "READY ${it} ms" },
            diagnostics.firstFrameMs?.let { "1º frame ${it} ms" },
            if (!diagnostics.renderedFirstFrame) "sem 1º frame" else null,
        ).joinToString(" • ")
        if (timingLine.isNotBlank()) add(timingLine)

        diagnostics.lastWatchdogReason?.takeIf { it.isNotBlank() }?.let { add("Watchdog: ${it.shorten()}") }
        diagnostics.lastReconnectReason?.takeIf { it.isNotBlank() }?.let { add("Reconnect: ${it.shorten()}") }
        diagnostics.lastError?.takeIf { it.isNotBlank() }?.let { add("Erro: ${it.shorten()}") }
    }

    private fun PlayerDiagnostics.transportLabel(): String =
        if (initialTransportMode != transportMode) {
            "${initialTransportMode.shortLabel()} -> ${transportMode.shortLabel()} fallback"
        } else {
            transportMode.shortLabel()
        }

    private fun PlayerDiagnostics.codecLabel(): String? {
        val parts = listOfNotNull(
            mimeType?.removePrefix("video/")?.uppercase(),
            codecs,
        )
        return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ")
    }

    private fun PlayerDiagnostics.decoderLabel(): String {
        val name = decoderName ?: return if (decoderFallbackEnabled) "SW fallback" else "desconhecido"
        val software = decoderFallbackEnabled ||
            name.contains("google", ignoreCase = true) ||
            name.contains("android", ignoreCase = true) ||
            name.contains("software", ignoreCase = true) ||
            name.contains("ffmpeg", ignoreCase = true)
        return "${if (software) "SW" else "HW"} • $name"
    }

    private fun RtspTransportMode.shortLabel(): String = when (this) {
        RtspTransportMode.UdpFirst -> "UDP"
        RtspTransportMode.TcpOnly -> "TCP"
    }

    private fun Float.clean(): String =
        if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)

    private fun Long.kbps(): Long = (this / 1_000L).coerceAtLeast(1L)

    private fun String.shorten(maxLength: Int = 44): String =
        if (length <= maxLength) this else take(maxLength - 1) + "…"
}
