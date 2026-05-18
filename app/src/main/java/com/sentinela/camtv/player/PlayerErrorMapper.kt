package com.sentinela.camtv.player

import androidx.media3.common.PlaybackException

object PlayerErrorMapper {
    fun map(
        error: PlaybackException,
        transportMode: RtspTransportMode,
    ): PlayerConnectionState {
        val details = error.diagnosticsText().lowercase()

        return when {
            details.hasAny("401", "unauthorized", "authentication", "auth", "permission") ->
                PlayerConnectionState.AuthenticationFailed
            details.hasAny("codec", "decoder", "decoding", "format_unsupported", "exceeds_capabilities") ->
                PlayerConnectionState.UnsupportedCodec
            details.hasAny("timeout", "timed out") && transportMode == RtspTransportMode.UdpFirst ->
                PlayerConnectionState.UdpLikelyBlocked
            details.hasAny("timeout", "timed out") ->
                PlayerConnectionState.Timeout
            details.hasAny("network", "unreachable", "unknownhost", "econnrefused", "enetunreach") ->
                PlayerConnectionState.NetworkOffline
            else -> PlayerConnectionState.UnknownError(error.shortMessage())
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
