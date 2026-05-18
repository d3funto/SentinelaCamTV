package com.sentinela.camtv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = SentinelaPrimary,
    onPrimary = SentinelaOnPrimary,
    background = SentinelaBackground,
    onBackground = SentinelaOnSurface,
    surface = SentinelaSurface,
    onSurface = SentinelaOnSurface,
    surfaceVariant = SentinelaSurfaceVariant,
    onSurfaceVariant = SentinelaOnSurfaceVariant,
    error = SentinelaError,
)

@Composable
fun SentinelaCamTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
