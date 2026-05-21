package com.sentinela.camtv.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme

object SentinelaTvSpacing {
    val xSmall: Dp = 4.dp
    val small: Dp = 7.dp
    val medium: Dp = 10.dp
    val large: Dp = 16.dp
    val xLarge: Dp = 22.dp
    val screenHorizontal: Dp = 56.dp
    val screenVertical: Dp = 40.dp
}

object SentinelaTvSize {
    val buttonMinHeight: Dp = 44.dp
    val tabMinHeight: Dp = 48.dp
    val fieldMinHeight: Dp = 44.dp
    val panelMinHeight: Dp = 420.dp
    val focusBorder: Dp = 2.dp
}

object SentinelaTvPadding {
    val panel = PaddingValues(18.dp)
}

object SentinelaTvColors {
    val panel = Color(0xFF0B2833)
    val panelBorder = Color(0xFF315766)
    val control = Color(0xFF123D4C)
    val controlFocused = Color(0xFF22D3F5)
    val controlSelected = Color(0xFF16495A)
    val field = Color(0xFF123A48)
    val fieldBorder = Color(0xFF5F8390)
    val mutedText = Color(0xFFB8D3DB)
    val divider = Color(0xFF1E4654)
}

@Composable
fun SentinelaPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(SentinelaTvColors.panel)
            .border(
                width = 1.dp,
                color = SentinelaTvColors.panelBorder,
            )
            .padding(SentinelaTvPadding.panel),
        content = content,
    )
}

@Composable
fun Modifier.sentinelaSelectedBorder(selected: Boolean): Modifier =
    border(
        width = if (selected) SentinelaTvSize.focusBorder else 1.dp,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
    )

fun Modifier.sentinelaButtonSize(): Modifier =
    defaultMinSize(minHeight = SentinelaTvSize.buttonMinHeight)
