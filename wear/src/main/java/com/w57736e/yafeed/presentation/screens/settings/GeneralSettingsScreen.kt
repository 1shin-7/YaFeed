package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*

@Composable
fun GeneralSettingsScreen(
    maxCacheSize: Int,
    updateInterval: Long,
    browserType: String,
    browserAvailable: Boolean,
    availableBrowsers: List<String>,
    notificationEnabled: Boolean,
    onMaxCacheSizeChange: (Int) -> Unit,
    onUpdateIntervalChange: (Long) -> Unit,
    onBrowserTypeChange: (String) -> Unit,
    onNotificationEnabledChange: (Boolean) -> Unit
) {
    val scrollState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text("General Settings", style = MaterialTheme.typography.titleMedium)
            }

            item {
                SwitchButton(
                    checked = notificationEnabled,
                    onCheckedChange = onNotificationEnabledChange,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text("Notifications") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        "Cache Size: $maxCacheSize",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = maxCacheSize.toFloat(),
                        onValueChange = { onMaxCacheSizeChange(it.toInt()) },
                        valueRange = 5f..50f,
                        steps = 44,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Text(
                    "Update Interval(min)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            item {
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = { onUpdateIntervalChange(5) },
                        modifier = Modifier.weight(1f),
                        colors = if (updateInterval == 5L) ButtonDefaults.filledTonalButtonColors()
                               else ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("5")
                        }
                    }
                    FilledTonalButton(
                        onClick = { onUpdateIntervalChange(10) },
                        modifier = Modifier.weight(1f),
                        colors = if (updateInterval == 10L) ButtonDefaults.filledTonalButtonColors()
                               else ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("10")
                        }
                    }
                    FilledTonalButton(
                        onClick = { onUpdateIntervalChange(15) },
                        modifier = Modifier.weight(1f),
                        colors = if (updateInterval == 15L) ButtonDefaults.filledTonalButtonColors()
                               else ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("15")
                        }
                    }
                }
            }

            item {
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = { onUpdateIntervalChange(30) },
                        modifier = Modifier.weight(1f),
                        colors = if (updateInterval == 30L) ButtonDefaults.filledTonalButtonColors()
                               else ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("30")
                        }
                    }
                    FilledTonalButton(
                        onClick = { onUpdateIntervalChange(60) },
                        modifier = Modifier.weight(1f),
                        colors = if (updateInterval == 60L) ButtonDefaults.filledTonalButtonColors()
                               else ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("60")
                        }
                    }
                }
            }

            if (browserAvailable && availableBrowsers.size > 1) {
                item {
                    Text(
                        "Browser",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                item {
                    ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                        availableBrowsers.forEach { browser ->
                            FilledTonalButton(
                                onClick = { onBrowserTypeChange(browser) },
                                modifier = Modifier.weight(1f),
                                colors = if (browserType == browser) ButtonDefaults.filledTonalButtonColors()
                                       else ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text(when (browser) {
                                        "default" -> "Default"
                                        "samsung" -> "Samsung"
                                        "chrome" -> "Chrome"
                                        else -> browser
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
