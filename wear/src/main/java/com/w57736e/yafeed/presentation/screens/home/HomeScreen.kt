package com.w57736e.yafeed.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.presentation.components.SourceCard
import com.w57736e.yafeed.presentation.components.SourceGridItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSourceClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScalingLazyListState()

    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = { viewModel.toggleViewMode() }
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.isGridView) Icons.Default.Menu else Icons.Default.Apps,
                        contentDescription = null
                    )
                    Text(if (uiState.isGridView) "List" else "Grid")
                }
            }
        }
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refreshAll() }
        ) {
            ScalingLazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    start = contentPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
                )
            ) {
                item {
                    Text(
                        "YaFeed",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (uiState.isGridView) {
                    // Manual 2-column grid in TransformingLazyColumn
                    val chunkedSources = uiState.sources.chunked(2)
                    items(chunkedSources) { rowSources ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSources.forEach { source ->
                                SourceGridItem(
                                    source = source,
                                    onClick = { onSourceClick(source.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Add spacer if the row only has one item
                            if (rowSources.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(uiState.sources) { source ->
                        SourceCard(
                            source = source,
                            onClick = { onSourceClick(source.id) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                item {
                    Button(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Settings")
                    }
                }
            }
        }
    }
}
