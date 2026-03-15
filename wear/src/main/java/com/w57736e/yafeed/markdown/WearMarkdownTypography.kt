package com.w57736e.yafeed.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.wear.compose.material3.MaterialTheme
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownTypography

@Composable
fun wearMarkdownTypography(
    h1: TextStyle = MaterialTheme.typography.displayLarge,
    h2: TextStyle = MaterialTheme.typography.displayMedium,
    h3: TextStyle = MaterialTheme.typography.displaySmall,
    h4: TextStyle = MaterialTheme.typography.titleMedium,
    h5: TextStyle = MaterialTheme.typography.titleSmall,
    h6: TextStyle = MaterialTheme.typography.titleLarge,
    text: TextStyle = MaterialTheme.typography.bodyLarge,
    code: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    quote: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
    paragraph: TextStyle = MaterialTheme.typography.bodyLarge,
    ordered: TextStyle = MaterialTheme.typography.bodyLarge,
    bullet: TextStyle = MaterialTheme.typography.bodyLarge,
    list: TextStyle = MaterialTheme.typography.bodyLarge,
    inlineCode: TextStyle = text.copy(fontFamily = FontFamily.Monospace),
    link: TextLinkStyles = TextLinkStyles(
        style = MaterialTheme.typography.bodyLarge.toSpanStyle()
    ),
    table: TextStyle = MaterialTheme.typography.bodyLarge,
): MarkdownTypography = DefaultMarkdownTypography(
    h1 = h1, h2 = h2, h3 = h3, h4 = h4, h5 = h5, h6 = h6,
    text = text, code = code, quote = quote, paragraph = paragraph,
    ordered = ordered, bullet = bullet, list = list,
    inlineCode = inlineCode, textLink = link, table = table
)
