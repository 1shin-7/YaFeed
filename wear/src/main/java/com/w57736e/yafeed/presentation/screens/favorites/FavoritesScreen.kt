package com.w57736e.yafeed.presentation.screens.favorites

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.R
import com.w57736e.yafeed.presentation.components.ArticleCard

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onArticleClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = scrollState) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            TransformingLazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    start = contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    end = contentPadding.calculateRightPadding(LayoutDirection.Ltr),
                    bottom = 64.dp
                )
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
                                text = { Text(stringResource(R.string.delete)) },
                                modifier = Modifier.fillMaxHeight()
                            )
                        },
                        onSwipePrimaryAction = { viewModel.deleteFavorite(favorite) },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .animateItem()
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
                        Text(stringResource(R.string.undo))
                    }
                }
            }
        }
    }
}

