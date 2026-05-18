package com.sentinela.camtv.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconnectBackoffPolicyTest {
    @Test
    fun delayUsesBaseDelayAndJitter() {
        val policy = ReconnectBackoffPolicy(jitterWindowMs = 0L)

        assertEquals(1000L, policy.delayForAttempt(0, "cam-1"))
        assertEquals(2000L, policy.delayForAttempt(1, "cam-1"))
        assertEquals(5000L, policy.delayForAttempt(2, "cam-1"))
    }

    @Test
    fun delayDoesNotExceedMaximum() {
        val policy = ReconnectBackoffPolicy()

        assertTrue(policy.delayForAttempt(99, "cam-1") <= 30000L)
    }
}
