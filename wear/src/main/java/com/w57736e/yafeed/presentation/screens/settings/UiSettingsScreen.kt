package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*

@Composable
fun UiSettingsScreen(
    showImages: Boolean,
    fontSize: Float,
    onShowImagesChange: (Boolean) -> Unit,
    onFontSizeChange: (Float) -> Unit
) {
    val scrollState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text("UI Settings", style = MaterialTheme.typography.titleMedium)
            }

            item {
                CheckboxButton(
                    checked = showImages,
                    onCheckedChange = onShowImagesChange,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Text("Show Images")
                        }
                    }
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        "Font Size: ${"%.1f".format(fontSize)}x",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = fontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 0.8f..1.5f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
