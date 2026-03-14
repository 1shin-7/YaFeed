package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch

@Composable
fun AddSourceScreen(
    onAddSource: suspend (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberTransformingLazyColumnState()
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = {
                    if (url.isNotBlank() && !isLoading) {
                        scope.launch {
                            isLoading = true
                            onAddSource(url, name)
                            isLoading = false
                            onNavigateBack()
                        }
                    }
                },
                enabled = url.isNotBlank() && !isLoading
            ) {
                Icon(Icons.Default.Check, contentDescription = "Add")
            }
        }
    ) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 64.dp)
        ) {
            item {
                Text("Add RSS Source", style = MaterialTheme.typography.titleMedium)
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("URL", style = MaterialTheme.typography.labelSmall)
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
                            value = url,
                            onValueChange = { url = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("Name (Optional)", style = MaterialTheme.typography.labelSmall)
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
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            singleLine = true
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
