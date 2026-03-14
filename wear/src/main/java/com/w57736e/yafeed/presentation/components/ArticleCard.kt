package com.w57736e.yafeed.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.data.repository.DateUtils
import com.w57736e.yafeed.data.repository.HtmlUtils
import com.w57736e.yafeed.domain.model.RssArticle

@Composable
fun ArticleCard(
    article: RssArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = DateUtils.formatRssDate(article.pubDate)
    val cleanTitle = HtmlUtils.stripHtml(article.title)
    val footer = if (article.author != null) "${article.author} • $formattedDate" else formattedDate

    TitleCard(
        onClick = onClick,
        title = {
            Text(
                cleanTitle, 
                maxLines = 2, 
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            ) 
        },
        subtitle = {
            Text(
                footer, 
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier.fillMaxWidth()
    )
}
