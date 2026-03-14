package com.w57736e.yafeed.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun YaFeedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        // Use default color scheme if darkColorScheme is unresolved
        content = content
    )
}