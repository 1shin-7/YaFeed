package com.w57736e.yafeed.presentation.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.presentation.components.ArticleCard

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onArticleClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    ScreenScaffold(scrollState = scrollState) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                autoCentering = if (uiState.favorites.isEmpty()) null else androidx.wear.compose.foundation.lazy.AutoCenteringParams()
            ) {
                item {
                    Text(
                        "Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                items(
                    items = uiState.favorites,
                    key = { it.link }
                ) { favorite ->
                    SwipeToReveal(
                        primaryAction = {
                            PrimaryActionButton(
                                onClick = { viewModel.deleteFavorite(favorite) },
                                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                text = { Text("Delete") },
                                modifier = Modifier.fillMaxHeight()
                            )
                        },
                        onSwipePrimaryAction = { viewModel.deleteFavorite(favorite) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        ArticleCard(
                            article = favorite.toRssArticle(),
                            onClick = { onArticleClick(favorite.link) }
                        )
                    }
                }
            }

            uiState.undoState?.let {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(onClick = { viewModel.undoDelete() }) {
                        Text("Undo")
                    }
                }
            }
        }
    }
}

