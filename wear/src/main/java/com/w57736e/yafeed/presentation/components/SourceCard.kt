package com.w57736e.yafeed.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.w57736e.yafeed.domain.model.RssSource
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SourceCard(
    source: RssSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val updateTime = if (source.lastUpdate > 0) timeFormat.format(Date(source.lastUpdate)) else ""

    AppCard(
        onClick = onClick,
        appName = {
            Text(source.name) 
        },
        appImage = {
            if (source.faviconUrl != null) {
                AsyncImage(
                    model = source.faviconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        title = { 
            Text(
                source.latestTitle ?: "No updates",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ) 
        },
        time = { 
            Text(updateTime) 
        },
        modifier = modifier.fillMaxWidth()
    ) {
        // Content area (ColumnScope)
    }
}
