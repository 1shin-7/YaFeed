package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.R
import com.w57736e.yafeed.domain.model.RssSource
import com.w57736e.yafeed.presentation.components.SplitActionButton

@Composable
fun SourcesScreen(
    sources: List<RssSource>,
    onAddSource: (String, String) -> Unit,
    onDeleteSource: (RssSource) -> Unit,
    onEditSource: (Int) -> Unit,
    onNavigateToAddSource: () -> Unit
) {
    val scrollState = rememberTransformingLazyColumnState()
    var sourceToDelete by remember { mutableStateOf<RssSource?>(null) }

    sourceToDelete?.let { source ->
        Dialog(onDismissRequest = { sourceToDelete = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Delete ${source.name}?",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.CenterHorizontally)
                ) {
                    FilledIconButton(
                        onClick = { sourceToDelete = null }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                    FilledIconButton(
                        onClick = {
                            onDeleteSource(source)
                            sourceToDelete = null
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm")
                    }
                }
            }
            }
        }
    }

    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(onClick = onNavigateToAddSource) {
                Text(stringResource(R.string.add_source))
            }
        }
    ) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 64.dp)
        ) {
            item {
                Text(stringResource(R.string.news_sources), style = MaterialTheme.typography.titleMedium)
            }

            items(
                items = sources,
                key = { it.id }
            ) { source ->
                SplitActionButton(
                    label = source.name,
                    onContainerClick = { onEditSource(source.id) },
                    onActionClick = { sourceToDelete = source },
                    actionIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        }
    }
}
