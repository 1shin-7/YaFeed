package com.w57736e.yafeed.presentation.screens.news_detail

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.domain.model.RssArticle

@Composable
fun NewsDetailScreen(
    article: RssArticle,
    showImages: Boolean
) {
    val scrollState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    article.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                val content = article.content ?: "No content available."
                // Markdown placeholder if library is unresolved
                Text(
                    content,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    "Link: ${article.link}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
