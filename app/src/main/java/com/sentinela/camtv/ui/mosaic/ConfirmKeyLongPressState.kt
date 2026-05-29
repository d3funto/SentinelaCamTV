package com.sentinela.camtv.ui.mosaic

internal enum class ConfirmKeyPressAction {
    None,
    Click,
    LongClick,
}

internal class ConfirmKeyLongPressState(
    private val longPressMs: Long,
) {
    private var downTimeMs: Long? = null
    private var longClickHandled = false

    fun onKeyDown(
        downTimeMs: Long,
        eventTimeMs: Long,
        repeatCount: Int,
    ): ConfirmKeyPressAction {
        if (this.downTimeMs == null) {
            this.downTimeMs = downTimeMs.takeIf { it > 0L } ?: eventTimeMs
            longClickHandled = false
        }

        val elapsedMs = eventTimeMs - (this.downTimeMs ?: eventTimeMs)
        return if (!longClickHandled && repeatCount > 0 && elapsedMs >= longPressMs) {
            longClickHandled = true
            ConfirmKeyPressAction.LongClick
        } else {
            ConfirmKeyPressAction.None
        }
    }

    fun onKeyUp(eventTimeMs: Long): ConfirmKeyPressAction {
        val startedAtMs = downTimeMs ?: return ConfirmKeyPressAction.None
        val elapsedMs = eventTimeMs - startedAtMs
        val action = when {
            longClickHandled -> ConfirmKeyPressAction.None
            elapsedMs >= longPressMs -> {
                longClickHandled = true
                ConfirmKeyPressAction.LongClick
            }
            else -> ConfirmKeyPressAction.Click
        }
        resetCurrentPress()
        return action
    }

    fun onTimerElapsed(eventTimeMs: Long): ConfirmKeyPressAction {
        val startedAtMs = downTimeMs ?: return ConfirmKeyPressAction.None
        return if (!longClickHandled && eventTimeMs - startedAtMs >= longPressMs) {
            longClickHandled = true
            ConfirmKeyPressAction.LongClick
        } else {
            ConfirmKeyPressAction.None
        }
    }

    fun reset() {
        resetCurrentPress()
    }

    private fun resetCurrentPress() {
        downTimeMs = null
        longClickHandled = false
    }
}
