package com.w57736e.yafeed.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.presentation.components.SourceCard
import com.w57736e.yafeed.presentation.components.SourceGridItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSourceClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = { viewModel.toggleViewMode() }
            ) {
                Text(if (uiState.isGridView) "List View" else "Grid View")
            }
        }
    ) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 64.dp)
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
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text("Settings")
                }
            }
        }
    }
}
