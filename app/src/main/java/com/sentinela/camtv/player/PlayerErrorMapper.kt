package com.sentinela.camtv.player

import androidx.media3.common.PlaybackException

object PlayerErrorMapper {
    fun map(
        error: PlaybackException,
        transportMode: RtspTransportMode,
    ): PlayerConnectionState {
        val details = error.diagnosticsText()

        return mapDiagnosticsText(
            details = details,
            transportMode = transportMode,
            fallbackMessage = error.shortMessage(),
        )
    }

    internal fun mapDiagnosticsText(
        details: String,
        transportMode: RtspTransportMode,
        fallbackMessage: String = "Erro desconhecido",
    ): PlayerConnectionState {
        val normalizedDetails = details.lowercase()

        return when {
            normalizedDetails.hasAny("401", "unauthorized", "authentication", "auth", "permission") ->
                PlayerConnectionState.AuthenticationFailed
            normalizedDetails.hasAny("codec", "decoder", "decoding", "format_unsupported", "exceeds_capabilities") ->
                PlayerConnectionState.UnsupportedCodec
            normalizedDetails.hasAny("timeout", "timed out") && transportMode == RtspTransportMode.UdpFirst ->
                PlayerConnectionState.UdpLikelyBlocked
            normalizedDetails.hasAny("timeout", "timed out") ->
                PlayerConnectionState.Timeout
            normalizedDetails.hasAny("econnrefused", "connection refused", "connectexception") ->
                PlayerConnectionState.ConnectionRefused
            normalizedDetails.hasAny("source error", "error_code_io_unspecified") ->
                PlayerConnectionState.UnknownError("Erro: falha ao abrir o fluxo RTSP")
            normalizedDetails.hasAny("network", "unreachable", "unknownhost", "enetunreach") ->
                PlayerConnectionState.NetworkOffline
            else -> PlayerConnectionState.UnknownError(fallbackMessage)
        }
    }

    fun isCodecFailure(error: PlaybackException): Boolean =
        map(error, RtspTransportMode.TcpOnly) == PlayerConnectionState.UnsupportedCodec
}

private fun String.hasAny(vararg needles: String): Boolean =
    needles.any { needle -> contains(needle) }

private fun PlaybackException.diagnosticsText(): String {
    val causeChain = generateSequence(cause) { throwable -> throwable.cause }
        .take(4)
        .joinToString(separator = " <- ") { throwable ->
            "${throwable.javaClass.simpleName}: ${throwable.message.orEmpty()}"
        }
    return "$errorCodeName ${message.orEmpty()} $causeChain"
}

fun PlaybackException.shortMessage(): String =
    "$errorCodeName: ${message ?: "erro sem mensagem"}"
