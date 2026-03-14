package com.w57736e.yafeed.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

private val YaFeedColorScheme = ColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE6E1E5),
    surfaceContainer = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun YaFeedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YaFeedColorScheme,
        content = content
    )
}
