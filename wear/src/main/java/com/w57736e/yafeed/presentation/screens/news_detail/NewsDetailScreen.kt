package com.w57736e.yafeed.presentation.screens.news_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.data.repository.DateUtils
import com.w57736e.yafeed.presentation.components.ImageViewer
import com.w57736e.yafeed.presentation.components.ContentRenderer

@Composable
fun NewsDetailScreen(
    article: RssArticle
) {
    val scrollState = rememberTransformingLazyColumnState()
    val formattedDate = remember(article.pubDate) { DateUtils.formatRssDateFull(article.pubDate) }
    val content = article.content ?: "No content available."
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showTimeText by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            showTimeText = true
        } else {
            kotlinx.coroutines.delay(2000)
            showTimeText = false
        }
    }

    selectedImageUrl?.let { imageUrl ->
        Dialog(onDismissRequest = { selectedImageUrl = null }) {
            ImageViewer(
                imageUrl = imageUrl,
                onDismiss = { selectedImageUrl = null }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(
            scrollState = scrollState,
            edgeButton = {
            EdgeButton(
                onClick = { /* TODO: Open in browser */ }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "View",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    ) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 0.dp, bottom = 64.dp)
        ) {
            // 顶部图片 + 渐变遮罩 + 标题 复合项
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // 1. 底层图片
                    if (article.imageUrl != null) {
                        AsyncImage(
                            model = article.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 无图时的保底背景色
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer))
                    }

                    // 2. 渐变遮罩 (从透明到背景色)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black
                                    ),
                                    startY = 100f
                                )
                            )
                    )

                    // 3. 标题 (定位在底部)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                }
            }

            // 作者和日期 (添加水平 Padding)
            if (article.author != null || article.pubDate != null) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        article.author?.let { author ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    author,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        formattedDate?.let { date ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        article.categories?.takeIf { it.isNotEmpty() }?.let { categories ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                categories.take(3).forEach { category ->
                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 正文 (添加水平 Padding)
            item {
                ContentRenderer(
                    content = content,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    ),
                    onImageClick = { url -> selectedImageUrl = url },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
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
