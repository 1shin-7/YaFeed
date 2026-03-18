package com.w57736e.yafeed.presentation.screens.news_list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.R
import com.w57736e.yafeed.presentation.components.ArticleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    viewModel: NewsListViewModel,
    sourceId: Int,
    onArticleClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScalingLazyListState()

    LaunchedEffect(sourceId) {
        viewModel.setSourceId(sourceId)
    }

    ScreenScaffold(scrollState = scrollState) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refreshArticles() }
        ) {
            ScalingLazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                item {
                    Text(
                        uiState.source?.name ?: stringResource(R.string.latest_news),
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
                        stringResource(R.string.error_prefix, uiState.error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.articles,
                    key = { _, article -> article.link ?: article.title }
                ) { index, article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(index) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
        }
    }
}
