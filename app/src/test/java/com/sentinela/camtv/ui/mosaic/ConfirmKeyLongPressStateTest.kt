package com.sentinela.camtv.ui.mosaic

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfirmKeyLongPressStateTest {
    @Test
    fun keyUpAt150MsIsClick() {
        val state = ConfirmKeyLongPressState(longPressMs = 400L)

        assertEquals(
            ConfirmKeyPressAction.None,
            state.onKeyDown(downTimeMs = 1_000L, eventTimeMs = 1_000L, repeatCount = 0),
        )
        assertEquals(ConfirmKeyPressAction.Click, state.onKeyUp(eventTimeMs = 1_150L))
    }

    @Test
    fun keyUpAt399MsIsClick() {
        val state = ConfirmKeyLongPressState(longPressMs = 400L)

        state.onKeyDown(downTimeMs = 1_000L, eventTimeMs = 1_000L, repeatCount = 0)

        assertEquals(ConfirmKeyPressAction.Click, state.onKeyUp(eventTimeMs = 1_399L))
    }

    @Test
    fun keyUpAt400MsIsLongClick() {
        val state = ConfirmKeyLongPressState(longPressMs = 400L)

        state.onKeyDown(downTimeMs = 1_000L, eventTimeMs = 1_000L, repeatCount = 0)

        assertEquals(ConfirmKeyPressAction.LongClick, state.onKeyUp(eventTimeMs = 1_400L))
    }

    @Test
    fun timerAfterThresholdIsLongClick() {
        val state = ConfirmKeyLongPressState(longPressMs = 400L)

        state.onKeyDown(downTimeMs = 1_000L, eventTimeMs = 1_000L, repeatCount = 0)

        assertEquals(ConfirmKeyPressAction.LongClick, state.onTimerElapsed(eventTimeMs = 1_400L))
        assertEquals(ConfirmKeyPressAction.None, state.onKeyUp(eventTimeMs = 1_450L))
    }

    @Test
    fun repeatedKeyDownAfterThresholdIsLongClick() {
        val state = ConfirmKeyLongPressState(longPressMs = 400L)

        state.onKeyDown(downTimeMs = 1_000L, eventTimeMs = 1_000L, repeatCount = 0)

        assertEquals(
            ConfirmKeyPressAction.LongClick,
            state.onKeyDown(downTimeMs = 1_000L, eventTimeMs = 1_400L, repeatCount = 1),
        )
        assertEquals(ConfirmKeyPressAction.None, state.onKeyUp(eventTimeMs = 1_450L))
    }

    @Test
    fun repeatedShortClickSequenceIsNotLongClick() {
        val state = ConfirmKeyLongPressState(longPressMs = 400L)

        state.onKeyDown(downTimeMs = 1_000L, eventTimeMs = 1_000L, repeatCount = 0)
        assertEquals(ConfirmKeyPressAction.Click, state.onKeyUp(eventTimeMs = 1_150L))

        state.onKeyDown(downTimeMs = 1_500L, eventTimeMs = 1_500L, repeatCount = 0)
        assertEquals(ConfirmKeyPressAction.Click, state.onKeyUp(eventTimeMs = 1_650L))
    }
}
