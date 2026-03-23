package com.w57736e.yafeed.presentation.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.w57736e.yafeed.R
import com.w57736e.yafeed.sync.ConnectionStatus
import com.w57736e.yafeed.sync.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WearConnectionDebugScreen(viewModel: WearConnectionDebugViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val syncHistory by viewModel.syncHistory.collectAsState()
    val scrollState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 64.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.connection_status),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                ConnectionStatusCard(connectionState.status)
            }

            items(connectionState.nodes.size) { index ->
                val node = connectionState.nodes[index]
                NodeCard(node.displayName, node.isNearby)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.manual_sync),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Button(
                    onClick = { viewModel.syncSettings() },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(stringResource(R.string.sync_settings))
                }
            }

            item {
                Button(
                    onClick = { viewModel.syncSources() },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(stringResource(R.string.sync_sources))
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.test_actions),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Button(
                    onClick = { viewModel.openPageOnPhone() },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(stringResource(R.string.open_page_on_phone))
                }
            }

            item {
                Button(
                    onClick = { viewModel.requestTextInput() },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(stringResource(R.string.request_text_input))
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sync_history),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(syncHistory.take(10).size) { index ->
                val event = syncHistory.take(10)[index]
                SyncEventCard(event.timestamp, event.type.name, event.status, event.message)
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(status: ConnectionStatus) {
    Card(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val color = when (status) {
                ConnectionStatus.CONNECTED -> Color.Green
                ConnectionStatus.SEARCHING -> Color.Yellow
                ConnectionStatus.DISCONNECTED -> Color.Red
            }
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = status.name,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NodeCard(displayName: String, isNearby: Boolean) {
    Card(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = displayName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (isNearby) "Nearby" else "Remote",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SyncEventCard(timestamp: Long, type: String, status: SyncStatus, message: String) {
    Card(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = type, style = MaterialTheme.typography.labelLarge)
                val statusColor = when (status) {
                    SyncStatus.SUCCESS -> Color.Green
                    SyncStatus.FAILURE -> Color.Red
                    SyncStatus.IN_PROGRESS -> Color.Yellow
                }
                Text(
                    text = status.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor
                )
            }
            Text(
                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

