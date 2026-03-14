package com.w57736e.yafeed.ui.sources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w57736e.yafeed.domain.model.RssSource
import com.w57736e.yafeed.ui.components.SourceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    viewModel: SourcesViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddEdit: (RssSource?) -> Unit
) {
    val sources by viewModel.sources.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RSS Sources") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddEdit(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Source")
            }
        }
    ) { padding ->
        if (sources.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No sources yet. Add one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sources, key = { it.id }) { source ->
                    SourceItem(
                        source = source,
                        onEdit = { onNavigateToAddEdit(source) },
                        onDelete = { viewModel.deleteSource(source) }
                    )
                }
            }
        }
    }
}
