package com.w57736e.yafeed.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.MaterialTheme
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.MarkdownColors

@Composable
fun wearMarkdownColor(
    text: Color = MaterialTheme.colorScheme.onBackground,
    codeBackground: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
    inlineCodeBackground: Color = codeBackground,
    dividerColor: Color = MaterialTheme.colorScheme.outline,
    tableBackground: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f),
): MarkdownColors = DefaultMarkdownColors(
    text = text,
    codeBackground = codeBackground,
    inlineCodeBackground = inlineCodeBackground,
    dividerColor = dividerColor,
    tableBackground = tableBackground,
)
