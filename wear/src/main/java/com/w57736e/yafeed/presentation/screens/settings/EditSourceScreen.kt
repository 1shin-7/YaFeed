package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.launch

@Composable
fun EditSourceScreen(
    source: RssSource,
    onSave: suspend (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberTransformingLazyColumnState()
    var name by remember { mutableStateOf(source.name) }
    var notificationEnabled by remember { mutableStateOf(source.notificationEnabled) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = {
                    if (name.isNotBlank() && !isLoading) {
                        scope.launch {
                            isLoading = true
                            onSave(name, notificationEnabled)
                            isLoading = false
                            onNavigateBack()
                        }
                    }
                },
                enabled = name.isNotBlank() && !isLoading
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 64.dp)
        ) {
            item {
                Text("Edit Source", style = MaterialTheme.typography.titleMedium)
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("Name", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.shapes.small
                            )
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("URL", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        source.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                SwitchButton(
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    label = { Text("Notifications") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
