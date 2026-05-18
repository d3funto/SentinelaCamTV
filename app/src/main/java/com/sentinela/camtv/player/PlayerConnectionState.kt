package com.sentinela.camtv.player

sealed interface PlayerConnectionState {
    data object Connecting : PlayerConnectionState
    data object Buffering : PlayerConnectionState
    data object Playing : PlayerConnectionState
    data class Reconnecting(val message: String? = null) : PlayerConnectionState
    data object NetworkOffline : PlayerConnectionState
    data object AuthenticationFailed : PlayerConnectionState
    data object Timeout : PlayerConnectionState
    data object UnsupportedCodec : PlayerConnectionState
    data object UdpLikelyBlocked : PlayerConnectionState
    data class UnknownError(val message: String) : PlayerConnectionState
}

fun PlayerConnectionState.statusText(): String = when (this) {
    PlayerConnectionState.Connecting -> "Estado: conectando"
    PlayerConnectionState.Buffering -> "Estado: buffer"
    PlayerConnectionState.Playing -> "Estado: reproduzindo"
    is PlayerConnectionState.Reconnecting -> if (message.isNullOrBlank()) {
        "Estado: reconectando"
    } else {
        "Estado: reconectando - $message"
    }

    PlayerConnectionState.NetworkOffline -> "Erro: rede offline"
    PlayerConnectionState.AuthenticationFailed -> "Erro: login ou senha invalidos"
    PlayerConnectionState.Timeout -> "Erro: timeout"
    PlayerConnectionState.UnsupportedCodec -> "Erro: codec nao suportado"
    PlayerConnectionState.UdpLikelyBlocked -> "Aviso: UDP instavel, tente modo QUALIDADE"
    is PlayerConnectionState.UnknownError -> message
}

fun PlayerConnectionState.isTerminalError(): Boolean = when (this) {
    PlayerConnectionState.NetworkOffline,
    PlayerConnectionState.AuthenticationFailed,
    PlayerConnectionState.Timeout,
    PlayerConnectionState.UnsupportedCodec,
    PlayerConnectionState.UdpLikelyBlocked,
    is PlayerConnectionState.UnknownError -> true
    PlayerConnectionState.Connecting,
    PlayerConnectionState.Buffering,
    PlayerConnectionState.Playing,
    is PlayerConnectionState.Reconnecting -> false
}
