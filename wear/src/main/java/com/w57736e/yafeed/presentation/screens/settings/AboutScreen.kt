package com.w57736e.yafeed.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*

@Composable
fun AboutScreen() {
    val scrollState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text("About YaFeed", style = MaterialTheme.typography.titleMedium)
            }

            item {
                Text(
                    "YaFeed is a modern Wear OS RSS Reader built with Jetpack Compose Material 3 Expressive.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            item {
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            item {
                Text(
                    "Libraries: RSS-Parser, Compose Markdown, Coil, Room, DataStore.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
