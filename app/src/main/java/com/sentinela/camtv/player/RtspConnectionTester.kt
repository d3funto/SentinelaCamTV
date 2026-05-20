package com.sentinela.camtv.player

sealed interface RtspConnectionTestResult {
    data object Success : RtspConnectionTestResult
    data class Failure(val state: PlayerConnectionState) : RtspConnectionTestResult
}

interface RtspConnectionTester {
    suspend fun test(rtspUrl: String): RtspConnectionTestResult
}

fun RtspConnectionTestResult.userMessage(streamName: String): String? = when (this) {
    RtspConnectionTestResult.Success -> null
    is RtspConnectionTestResult.Failure -> "$streamName: ${state.statusText()}"
}
