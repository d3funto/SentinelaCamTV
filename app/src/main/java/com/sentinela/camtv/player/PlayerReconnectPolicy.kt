package com.sentinela.camtv.player

object PlayerReconnectPolicy {
    private const val TCP_FALLBACK_FAILURE_THRESHOLD = 2
    const val CONNECTING_WATCHDOG_MS = 12_000L
    const val FULLSCREEN_BUFFERING_WATCHDOG_MS = 8_000L
    const val MOSAIC_BUFFERING_WATCHDOG_MS = 12_000L
    const val FIRST_FRAME_WATCHDOG_MS = 5_000L

    fun transportModeForFailureCount(consecutiveFailures: Int): RtspTransportMode =
        if (consecutiveFailures >= TCP_FALLBACK_FAILURE_THRESHOLD) {
            RtspTransportMode.TcpOnly
        } else {
            RtspTransportMode.UdpFirst
        }

    fun watchdogTimeoutFor(
        mode: PlayerMode,
        state: PlayerConnectionState,
    ): Long? = when (state) {
        PlayerConnectionState.Connecting -> CONNECTING_WATCHDOG_MS
        PlayerConnectionState.Buffering -> when (mode) {
            PlayerMode.Mosaic -> MOSAIC_BUFFERING_WATCHDOG_MS
            PlayerMode.Fullscreen -> FULLSCREEN_BUFFERING_WATCHDOG_MS
        }

        PlayerConnectionState.Playing,
        PlayerConnectionState.NetworkOffline,
        PlayerConnectionState.ConnectionRefused,
        PlayerConnectionState.AuthenticationFailed,
        PlayerConnectionState.Timeout,
        PlayerConnectionState.UnsupportedCodec,
        PlayerConnectionState.UdpLikelyBlocked,
        is PlayerConnectionState.Reconnecting,
        is PlayerConnectionState.UnknownError -> null
    }

    fun firstFrameWatchdogTimeoutFor(mode: PlayerMode): Long = when (mode) {
        PlayerMode.Mosaic -> FIRST_FRAME_WATCHDOG_MS
        PlayerMode.Fullscreen -> FIRST_FRAME_WATCHDOG_MS
    }
}
