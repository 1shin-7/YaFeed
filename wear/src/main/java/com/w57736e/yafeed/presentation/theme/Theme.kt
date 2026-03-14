package com.w57736e.yafeed.presentation.theme

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

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
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamicScheme = androidx.compose.material3.dynamicDarkColorScheme(context)
            ColorScheme(
                primary = dynamicScheme.primary,
                onPrimary = dynamicScheme.onPrimary,
                secondary = dynamicScheme.secondary,
                onSecondary = dynamicScheme.onSecondary,
                background = dynamicScheme.background,
                onBackground = dynamicScheme.onBackground,
                surfaceContainer = dynamicScheme.surfaceContainer,
                onSurface = dynamicScheme.onSurface,
                onSurfaceVariant = dynamicScheme.onSurfaceVariant,
                outline = dynamicScheme.outline,
                outlineVariant = dynamicScheme.outlineVariant,
                error = dynamicScheme.error,
                onError = dynamicScheme.onError
            )
        }
        else -> YaFeedColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
