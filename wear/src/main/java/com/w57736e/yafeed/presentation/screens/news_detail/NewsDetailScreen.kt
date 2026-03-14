package com.w57736e.yafeed.presentation.screens.news_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.data.repository.DateUtils
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun NewsDetailScreen(
    article: RssArticle
) {
    val scrollState = rememberTransformingLazyColumnState()
    val formattedDate = remember(article.pubDate) { DateUtils.formatRssDateFull(article.pubDate) }
    val content = article.content ?: "No content available."
    
    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = { /* TODO: Open in browser */ }
            ) {
                Text("View Original")
            }
        }
    ) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            // 移除水平 Padding，让图片能铺满
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
                        MarkdownText(
                            markdown = article.title,
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
            if (article.pubDate != null || article.author != null) {
                item {
                    val info = listOfNotNull(article.author, formattedDate).joinToString(" • ")
                    Text(
                        info,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // 正文 (添加水平 Padding)
            item {
                MarkdownText(
                    markdown = content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}
