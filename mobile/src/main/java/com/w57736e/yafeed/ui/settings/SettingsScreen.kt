package com.w57736e.yafeed.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val showImages by viewModel.showImages.collectAsState()
    val updateInterval by viewModel.updateInterval.collectAsState()
    val maxCacheSize by viewModel.maxCacheSize.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val browserType by viewModel.browserType.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val saveImagesOnFavorite by viewModel.saveImagesOnFavorite.collectAsState()
    val useOriginalImagePreview by viewModel.useOriginalImagePreview.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSection(title = "Display") {
                    SettingsSwitch(
                        label = "Show Images",
                        checked = showImages,
                        onCheckedChange = { viewModel.setShowImages(it) }
                    )
                    SettingsSwitch(
                        label = "Use Original Image Preview",
                        checked = useOriginalImagePreview,
                        onCheckedChange = { viewModel.setUseOriginalImagePreview(it) }
                    )
                    SettingsSlider(
                        label = "Font Size",
                        value = fontSize,
                        onValueChange = { viewModel.setFontSize(it) },
                        valueRange = 7f..20f,
                        steps = 12,
                        valueLabel = "${fontSize.toInt()}sp"
                    )
                }
            }

            item {
                SettingsSection(title = "General") {
                    SettingsSwitch(
                        label = "Notifications",
                        checked = notificationEnabled,
                        onCheckedChange = { viewModel.setNotificationEnabled(it) }
                    )
                    SettingsSwitch(
                        label = "Save Images on Favorite",
                        checked = saveImagesOnFavorite,
                        onCheckedChange = { viewModel.setSaveImagesOnFavorite(it) }
                    )
                    SettingsSlider(
                        label = "Cache Size",
                        value = maxCacheSize.toFloat(),
                        onValueChange = { viewModel.setMaxCacheSize(it.toInt()) },
                        valueRange = 10f..200f,
                        steps = 18,
                        valueLabel = "$maxCacheSize articles"
                    )
                }
            }

            item {
                SettingsSection(
                    title = "Update Interval",
                    description = "How often to refresh RSS feeds"
                ) {
                    // Fine-grained slider: 5min to 24h (1440min)
                    SettingsSlider(
                        label = "Custom",
                        value = updateInterval.toFloat(),
                        onValueChange = { viewModel.setUpdateInterval(it.toLong()) },
                        valueRange = 5f..1440f,
                        steps = 0,
                        valueLabel = formatInterval(updateInterval)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Preset chips - using FlowRow for auto wrapping
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            5L to "5min",
                            10L to "10min",
                            15L to "15min",
                            30L to "30min",
                            60L to "1hr",
                            120L to "2hr",
                            180L to "3hr",
                            360L to "6hr",
                            1440L to "1day"
                        )
                        presets.forEach { (interval, label) ->
                            FilterChip(
                                selected = updateInterval == interval,
                                onClick = { viewModel.setUpdateInterval(interval) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            item {
                SettingsSection(
                    title = "Browser",
                    description = "Select browser for opening links"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("default", "samsung", "chrome").forEach { browser ->
                            FilterChip(
                                selected = browserType == browser,
                                onClick = { viewModel.setBrowserType(browser) },
                                label = { Text(browser.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
                
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatInterval(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}min"
        minutes < 1440 -> "${minutes / 60}hr"
        else -> "${minutes / 1440}day"
    }
}
