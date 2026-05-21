package com.sentinela.camtv.ui.player

import androidx.compose.ui.input.key.Key
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenQuickMenuKeyTest {
    @Test
    fun okAndEnterOpenQuickMenu() {
        assertTrue(Key.DirectionCenter.opensFullscreenQuickMenu())
        assertTrue(Key.Enter.opensFullscreenQuickMenu())
        assertTrue(Key.NumPadEnter.opensFullscreenQuickMenu())
    }

    @Test
    fun directionDownDoesNotOpenQuickMenu() {
        assertFalse(Key.DirectionDown.opensFullscreenQuickMenu())
    }
}
