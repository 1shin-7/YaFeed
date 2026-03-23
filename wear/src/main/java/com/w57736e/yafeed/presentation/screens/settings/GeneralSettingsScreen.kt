package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.w57736e.yafeed.R

@Composable
fun GeneralSettingsScreen(
    maxCacheSize: Int,
    updateInterval: Long,
    browserType: String,
    browserAvailable: Boolean,
    availableBrowsers: List<String>,
    notificationEnabled: Boolean,
    saveImagesOnFavorite: Boolean = false,
    onMaxCacheSizeChange: (Int) -> Unit,
    onUpdateIntervalChange: (Long) -> Unit,
    onBrowserTypeChange: (String) -> Unit,
    onNotificationEnabledChange: (Boolean) -> Unit,
    onSaveImagesOnFavoriteChange: (Boolean) -> Unit = {}
) {
    val scrollState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text(stringResource(R.string.general_settings), style = MaterialTheme.typography.titleMedium)
            }

            item {
                SwitchButton(
                    checked = notificationEnabled,
                    onCheckedChange = onNotificationEnabledChange,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text(stringResource(R.string.notifications)) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
            }

            item {
                SwitchButton(
                    checked = saveImagesOnFavorite,
                    onCheckedChange = onSaveImagesOnFavoriteChange,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text(stringResource(R.string.save_images_on_favorite)) },
                    secondaryLabel = { Text(stringResource(R.string.save_images_description)) }
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.cache_size, maxCacheSize),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        value = maxCacheSize.toFloat(),
                        onValueChange = { onMaxCacheSizeChange(it.toInt()) },
                        valueRange = 10f..200f,
                        steps = 18,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Text(
                    stringResource(R.string.update_interval),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                if (updateInterval !in listOf(5L, 10L, 15L, 30L, 60L)) {
                    Text(
                        stringResource(R.string.custom_interval, formatInterval(updateInterval)),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                        stringResource(R.string.browser),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                        textAlign = TextAlign.Center
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
                                        "default" -> stringResource(R.string.browser_default)
                                        "samsung" -> stringResource(R.string.browser_samsung)
                                        "chrome" -> stringResource(R.string.browser_chrome)
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

private fun formatInterval(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}min"
        minutes < 1440 -> "${minutes / 60}h"
        else -> "${minutes / 1440}d"
    }
}
