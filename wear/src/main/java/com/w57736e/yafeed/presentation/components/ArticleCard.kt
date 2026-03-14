package com.w57736e.yafeed.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.w57736e.yafeed.domain.model.RssArticle

@Composable
fun ArticleCard(
    article: RssArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TitleCard(
        onClick = onClick,
        title = { 
            Text(
                article.title, 
                maxLines = 2, 
                overflow = TextOverflow.Ellipsis 
            ) 
        },
        subtitle = {
            if (article.author != null) {
                Text(article.author, style = MaterialTheme.typography.bodySmall)
            }
        },
        time = {
            if (article.pubDate != null) {
                Text(article.pubDate, style = MaterialTheme.typography.bodySmall)
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        if (article.imageUrl != null) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}
