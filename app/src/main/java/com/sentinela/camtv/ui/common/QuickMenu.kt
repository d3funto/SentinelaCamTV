package com.sentinela.camtv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.sentinela.camtv.ui.design.SentinelaTvColors
import com.sentinela.camtv.ui.design.SentinelaTvShape

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
            .widthIn(min = 260.dp, max = 340.dp)
            .background(
                color = SentinelaTvColors.panel.copy(alpha = 0.94f),
                shape = SentinelaTvShape.dialog,
            )
            .border(
                width = 1.dp,
                color = SentinelaTvColors.panelBorder,
                shape = SentinelaTvShape.dialog,
            )
            .padding(18.dp)
            .focusGroup(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        actions.forEachIndexed { index, action ->
            QuickMenuButton(
                label = action.label,
                onClick = action.onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (index == 0) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

@Composable
private fun QuickMenuButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key.isConfirmKey()) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .semantics { role = Role.Button }
            .background(
                color = SentinelaTvColors.control,
                shape = SentinelaTvShape.control,
            )
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) SentinelaTvColors.controlFocused else Color.Transparent,
                shape = SentinelaTvShape.control,
            )
            .padding(horizontal = 18.dp, vertical = 13.dp)
            .focusable(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun Key.isConfirmKey(): Boolean =
    this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
