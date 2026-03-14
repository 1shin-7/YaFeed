package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*

@Composable
fun UiSettingsScreen(
    uiScale: Float,
    showImages: Boolean,
    updateInterval: Long,
    onUiScaleChange: (Float) -> Unit,
    onShowImagesChange: (Boolean) -> Unit,
    onUpdateIntervalChange: (Long) -> Unit
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
                    label = { Text("Show Images") }
                )
            }

            item {
                Text(
                    "UI Scale: ${"%.1f".format(uiScale)}x",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onUiScaleChange(uiScale - 0.1f) }, modifier = Modifier.weight(1f)) { Text("-") }
                    Button(onClick = { onUiScaleChange(uiScale + 0.1f) }, modifier = Modifier.weight(1f)) { Text("+") }
                }
            }

            item {
                Text(
                    "Update Interval: $updateInterval min",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onUpdateIntervalChange(15) }, modifier = Modifier.weight(1f)) { Text("15") }
                    Button(onClick = { onUpdateIntervalChange(30) }, modifier = Modifier.weight(1f)) { Text("30") }
                    Button(onClick = { onUpdateIntervalChange(60) }, modifier = Modifier.weight(1f)) { Text("60") }
                }
            }
        }
    }
}
