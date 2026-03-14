package com.w57736e.yafeed.presentation.screens.news_detail

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 64.dp)
        ) {
            // 标题：使用 MarkdownText 以支持标题内的 HTML 实体
            item {
                MarkdownText(
                    markdown = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 文章首图
            if (article.imageUrl != null) {
                item {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(bottom = 12.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 作者和日期
            if (article.pubDate != null || article.author != null) {
                item {
                    val info = listOfNotNull(article.author, formattedDate).joinToString(" • ")
                    Text(
                        info,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // 正文：全量使用 MarkdownText，支持内嵌图片
            item {
                MarkdownText(
                    markdown = content,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}
