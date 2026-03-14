package com.w57736e.yafeed.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.w57736e.yafeed.domain.model.RssSource

@Composable
fun SourceGridItem(
    source: RssSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (source.faviconUrl != null) {
                AsyncImage(
                    model = source.faviconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // Placeholder icon
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                source.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
