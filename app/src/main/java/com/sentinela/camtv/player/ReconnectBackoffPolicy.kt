package com.sentinela.camtv.player

import kotlin.math.abs

class ReconnectBackoffPolicy(
    private val baseDelaysMs: List<Long> = listOf(1000L, 2000L, 5000L, 10000L, 20000L, 30000L),
    private val maxDelayMs: Long = 30000L,
    private val jitterWindowMs: Long = 750L,
) {
    fun delayForAttempt(
        attempt: Int,
        jitterSeed: String,
    ): Long {
        val safeAttempt = attempt.coerceAtLeast(0)
        val baseDelay = baseDelaysMs.getOrElse(safeAttempt) { maxDelayMs }
            .coerceAtMost(maxDelayMs)
        val jitter = if (jitterWindowMs > 0) {
            abs((jitterSeed.hashCode() + safeAttempt) % jitterWindowMs.toInt()).toLong()
        } else {
            0L
        }

        return (baseDelay + jitter).coerceAtMost(maxDelayMs)
    }
}
