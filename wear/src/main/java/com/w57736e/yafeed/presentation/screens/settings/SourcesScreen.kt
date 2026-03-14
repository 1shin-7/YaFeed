package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.domain.model.RssSource

@Composable
fun SourcesScreen(
    sources: List<RssSource>,
    onAddSource: (String, String) -> Unit,
    onDeleteSource: (RssSource) -> Unit
) {
    val scrollState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text("News Sources", style = MaterialTheme.typography.titleMedium)
            }

            items(sources) { source ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(source.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDeleteSource(source) }) {
                        Text("X") 
                    }
                }
            }

            item {
                Button(
                    onClick = { /* Show add dialog */ },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Add Source")
                }
            }
        }
    }
}
