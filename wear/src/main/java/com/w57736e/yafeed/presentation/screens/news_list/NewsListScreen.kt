package com.w57736e.yafeed.presentation.screens.news_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.presentation.components.ArticleCard

@Composable
fun NewsListScreen(
    viewModel: NewsListViewModel,
    sourceId: Int,
    onArticleClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberTransformingLazyColumnState()
    var showTimeText by remember { mutableStateOf(false) }

    LaunchedEffect(sourceId) {
        viewModel.setSourceId(sourceId)
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            showTimeText = true
        } else {
            kotlinx.coroutines.delay(2000)
            showTimeText = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 28.dp, start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    uiState.source?.name ?: "Latest News",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (uiState.articles.isEmpty() && uiState.isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (uiState.articles.isEmpty() && uiState.error != null) {
                item {
                    Text(
                        "Error: ${uiState.error}", 
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                items(uiState.articles.indices.toList()) { index ->
                    val article = uiState.articles[index]
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(index) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }

        AnimatedVisibility(
            visible = showTimeText,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TimeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
