package com.sentinela.camtv.ui.player

import com.sentinela.camtv.player.PlayerConnectionState
import com.sentinela.camtv.player.RtspTransportMode
import com.sentinela.camtv.player.StreamQuality
import com.sentinela.camtv.player.TransmissionMode
import com.sentinela.camtv.player.statusText
import com.sentinela.camtv.ui.labels.transmissionModeLabel

internal enum class DecoderKind {
    Hardware,
    Software,
    Unknown,
}

internal object DecoderClassifier {
    fun classify(decoderName: String?): DecoderKind {
        val normalized = decoderName?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
            ?: return DecoderKind.Unknown
        return when {
            normalized.contains("google") ||
                normalized.contains("c2.android") ||
                normalized.contains("software") ||
                normalized.contains("ffmpeg") -> DecoderKind.Software

            normalized.contains("amlogic") ||
                normalized.contains("qcom") ||
                normalized.contains("qualcomm") ||
                normalized.contains("mediatek") ||
                normalized.contains("mtk") ||
                normalized.contains("exynos") ||
                normalized.contains("rockchip") ||
                normalized.contains("realtek") ||
                normalized.contains("hisilicon") ||
                normalized.contains("mstar") -> DecoderKind.Hardware

            else -> DecoderKind.Unknown
        }
    }
}

internal data class PlayerDiagnostics(
    val cameraName: String,
    val connectionState: PlayerConnectionState,
    val subtype: Int,
    val transmissionMode: TransmissionMode,
    val initialTransportMode: RtspTransportMode,
    val transportMode: RtspTransportMode,
    val decoderFallbackEnabled: Boolean,
    val autoQualityDowngraded: Boolean = false,
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
    fun overlayLines(
        diagnostics: PlayerDiagnostics,
        showPlayerInfo: Boolean,
    ): List<String> = if (showPlayerInfo) overlayLines(diagnostics) else emptyList()

    fun overlayLines(diagnostics: PlayerDiagnostics): List<String> = buildList {
        val quality = StreamQuality.entries.firstOrNull { it.subtype == diagnostics.subtype } ?: StreamQuality.SD
        val qualityLabel = if (diagnostics.autoQualityDowngraded && quality == StreamQuality.SD) {
            "SD auto"
        } else {
            quality.name
        }
        add("${diagnostics.cameraName} • $qualityLabel • subtype ${diagnostics.subtype}")

        val videoLine = listOfNotNull(
            diagnostics.videoSize,
            diagnostics.framesPerSecond?.takeIf { it > 0f }?.let { "${it.clean()} FPS" },
        ).joinToString(" • ")
        if (videoLine.isNotBlank()) add(videoLine)

        add("${diagnostics.connectionState.statusText().removePrefix("Estado: ")} • ${transmissionModeLabel(diagnostics.transmissionMode)}")
        add("RTSP: ${diagnostics.transportLabel()}")

        diagnostics.codecLabel()?.let { add("Codec: $it") }
        add("Decoder: ${diagnostics.decoderLabel()}")
        if (diagnostics.decoderFallbackEnabled) {
            add("Fallback: ${diagnostics.fallbackLabel()}")
        }

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
        val name = decoderName ?: return "aguardando"
        val kind = when (decoderKind()) {
            DecoderKind.Hardware -> "HW"
            DecoderKind.Software -> "SW"
            DecoderKind.Unknown -> "?"
        }
        return "$kind • $name"
    }

    private fun PlayerDiagnostics.decoderKind(): DecoderKind =
        DecoderClassifier.classify(decoderName)

    private fun PlayerDiagnostics.fallbackLabel(): String =
        if (decoderKind() == DecoderKind.Software) "em uso" else "permitido"

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
