package com.sentinela.camtv.ui.cameras

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class TextFieldCursorControllerTest {
    @Test
    fun moveLeftMovesCollapsedCursor() {
        val value = TextFieldValue(
            text = "rtsp",
            selection = TextRange(3),
        )

        val moved = TextFieldCursorController.moveCursor(
            value = value,
            direction = CursorMoveDirection.Left,
        )

        assertEquals(TextRange(2), moved.selection)
    }

    @Test
    fun moveRightMovesCollapsedCursor() {
        val value = TextFieldValue(
            text = "rtsp",
            selection = TextRange(1),
        )

        val moved = TextFieldCursorController.moveCursor(
            value = value,
            direction = CursorMoveDirection.Right,
        )

        assertEquals(TextRange(2), moved.selection)
    }

    @Test
    fun cursorDoesNotMovePastTextEdges() {
        val start = TextFieldValue(
            text = "rtsp",
            selection = TextRange(0),
        )
        val end = TextFieldValue(
            text = "rtsp",
            selection = TextRange(4),
        )

        assertEquals(
            TextRange(0),
            TextFieldCursorController.moveCursor(
                value = start,
                direction = CursorMoveDirection.Left,
            ).selection,
        )
        assertEquals(
            TextRange(4),
            TextFieldCursorController.moveCursor(
                value = end,
                direction = CursorMoveDirection.Right,
            ).selection,
        )
    }

    @Test
    fun movingSelectionCollapsesToExpectedEdge() {
        val value = TextFieldValue(
            text = "rtsp://camera",
            selection = TextRange(2, 8),
        )

        assertEquals(
            TextRange(2),
            TextFieldCursorController.moveCursor(
                value = value,
                direction = CursorMoveDirection.Left,
            ).selection,
        )
        assertEquals(
            TextRange(8),
            TextFieldCursorController.moveCursor(
                value = value,
                direction = CursorMoveDirection.Right,
            ).selection,
        )
    }

    @Test
    fun syncExternalTextKeepsCursorInsideNewText() {
        val value = TextFieldValue(
            text = "rtsp://antigo",
            selection = TextRange(7),
        )

        val synced = TextFieldCursorController.syncExternalText(
            current = value,
            newText = "rtsp://novo/canal/1",
        )

        assertEquals("rtsp://novo/canal/1", synced.text)
        assertEquals(TextRange(7), synced.selection)
    }

    @Test
    fun syncExternalTextFromBlankMovesCursorToEnd() {
        val value = TextFieldValue(
            text = "",
            selection = TextRange(0),
        )

        val synced = TextFieldCursorController.syncExternalText(
            current = value,
            newText = "rtsp://camera/sub",
        )

        assertEquals("rtsp://camera/sub", synced.text)
        assertEquals(TextRange("rtsp://camera/sub".length), synced.selection)
    }

    @Test
    fun focusedFieldDoesNotMoveCursorBeforeEditing() {
        assertFalse(
            TextFieldEditModePolicy.shouldMoveCursor(
                isEditing = false,
                key = Key.DirectionRight,
            ),
        )
    }

    @Test
    fun confirmKeyEntersEditingOnlyWhenFocused() {
        assertTrue(
            TextFieldEditModePolicy.shouldEnterEditing(
                focused = true,
                isEditing = false,
                key = Key.DirectionCenter,
            ),
        )
        assertFalse(
            TextFieldEditModePolicy.shouldEnterEditing(
                focused = false,
                isEditing = false,
                key = Key.DirectionCenter,
            ),
        )
        assertFalse(
            TextFieldEditModePolicy.shouldEnterEditing(
                focused = true,
                isEditing = true,
                key = Key.DirectionCenter,
            ),
        )
    }

    @Test
    fun editingFieldConsumesHorizontalCursorKeys() {
        assertTrue(
            TextFieldEditModePolicy.shouldMoveCursor(
                isEditing = true,
                key = Key.DirectionLeft,
            ),
        )
        assertTrue(
            TextFieldEditModePolicy.shouldMoveCursor(
                isEditing = true,
                key = Key.DirectionRight,
            ),
        )
        assertFalse(
            TextFieldEditModePolicy.shouldMoveCursor(
                isEditing = true,
                key = Key.DirectionUp,
            ),
        )
    }
}
