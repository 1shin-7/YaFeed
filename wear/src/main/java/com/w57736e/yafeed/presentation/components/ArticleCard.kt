package com.w57736e.yafeed.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.data.repository.DateUtils
import com.w57736e.yafeed.domain.model.RssArticle
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ArticleCard(
    article: RssArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = DateUtils.formatRssDate(article.pubDate)
    val author = article.author ?: "Unknown"

    TitleCard(
        onClick = onClick,
        title = {
            // 使用 MarkdownText 处理标题，以防标题包含 HTML 实体
            MarkdownText(
                markdown = article.title,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        subtitle = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    author, 
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    formattedDate, 
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
