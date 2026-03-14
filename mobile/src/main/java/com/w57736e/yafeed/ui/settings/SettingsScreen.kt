package com.w57736e.yafeed.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiScale by viewModel.uiScale.collectAsState()
    val showImages by viewModel.showImages.collectAsState()
    val updateInterval by viewModel.updateInterval.collectAsState()
    val listViewGrid by viewModel.listViewGrid.collectAsState()
    val maxCacheSize by viewModel.maxCacheSize.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val browserType by viewModel.browserType.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("UI Scale: ${String.format("%.1f", uiScale)}", style = MaterialTheme.typography.titleSmall)
            Slider(
                value = uiScale,
                onValueChange = { viewModel.setUiScale(it) },
                valueRange = 0.5f..2.0f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Show Images")
                Switch(checked = showImages, onCheckedChange = { viewModel.setShowImages(it) })
            }

            Text("Update Interval: $updateInterval min", style = MaterialTheme.typography.titleSmall)
            Slider(
                value = updateInterval.toFloat(),
                onValueChange = { viewModel.setUpdateInterval(it.toInt()) },
                valueRange = 15f..120f,
                steps = 6
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grid View")
                Switch(checked = listViewGrid, onCheckedChange = { viewModel.setListViewGrid(it) })
            }

            Text("Cache Size: $maxCacheSize articles", style = MaterialTheme.typography.titleSmall)
            Slider(
                value = maxCacheSize.toFloat(),
                onValueChange = { viewModel.setMaxCacheSize(it.toInt()) },
                valueRange = 10f..100f,
                steps = 8
            )

            Text("Font Size: ${String.format("%.0f", fontSize)}sp", style = MaterialTheme.typography.titleSmall)
            Slider(
                value = fontSize,
                onValueChange = { viewModel.setFontSize(it) },
                valueRange = 10f..20f
            )

            Text("Browser Type", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = browserType == "default",
                    onClick = { viewModel.setBrowserType("default") },
                    label = { Text("Default") }
                )
                FilterChip(
                    selected = browserType == "custom",
                    onClick = { viewModel.setBrowserType("custom") },
                    label = { Text("Custom") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notifications")
                Switch(checked = notificationEnabled, onCheckedChange = { viewModel.setNotificationEnabled(it) })
            }
        }
    }
}
