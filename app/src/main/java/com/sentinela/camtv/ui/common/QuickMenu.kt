package com.sentinela.camtv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Text

data class QuickMenuAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun QuickMenu(
    actions: List<QuickMenuAction>,
    modifier: Modifier = Modifier,
) {
    val firstItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (actions.isNotEmpty()) {
            firstItemFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xEE101820))
            .padding(22.dp)
            .focusGroup(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        actions.forEachIndexed { index, action ->
            Button(
                onClick = action.onClick,
                modifier = Modifier
                    .then(
                        if (index == 0) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        },
                    )
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key.isConfirmKey()) {
                            action.onClick()
                            true
                        } else {
                            false
                        }
                    },
            ) {
                Text(action.label)
            }
        }
    }
}

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
