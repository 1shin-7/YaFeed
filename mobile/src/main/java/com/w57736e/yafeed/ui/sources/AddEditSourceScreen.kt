package com.w57736e.yafeed.ui.sources

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w57736e.yafeed.domain.model.RssSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSourceScreen(
    source: RssSource?,
    viewModel: SourcesViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(source?.name ?: "") }
    var url by remember { mutableStateOf(source?.url ?: "") }
    var notificationEnabled by remember { mutableStateOf(source?.notificationEnabled ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (source == null) "Add Source" else "Edit Source") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Feed URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Notifications")
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )
            }
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        if (source == null) {
                            viewModel.addSource(name.ifBlank { url }, url, notificationEnabled)
                        } else {
                            viewModel.updateSource(
                                source.copy(
                                    name = name.ifBlank { url },
                                    url = url,
                                    notificationEnabled = notificationEnabled
                                )
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
