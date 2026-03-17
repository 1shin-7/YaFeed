package com.w57736e.yafeed.sync

import android.content.Context
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class WearConnectionStateManager(context: Context) {
    private val nodeClient: NodeClient = Wearable.getNodeClient(context)

    private val _connectionState = MutableStateFlow(
        ConnectionState(
            status = ConnectionStatus.DISCONNECTED,
            nodes = emptyList(),
            lastCheckTime = System.currentTimeMillis()
        )
    )
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _syncHistory = MutableStateFlow<List<SyncEvent>>(emptyList())
    val syncHistory: StateFlow<List<SyncEvent>> = _syncHistory.asStateFlow()

    suspend fun startMonitoring() {
        while (true) {
            try {
                _connectionState.update { it.copy(status = ConnectionStatus.SEARCHING) }
                val nodes = nodeClient.connectedNodes.await()

                val connectedNodes = nodes.map { node ->
                    ConnectedNode(
                        id = node.id,
                        displayName = node.displayName,
                        isNearby = node.isNearby
                    )
                }

                _connectionState.update {
                    ConnectionState(
                        status = if (connectedNodes.isNotEmpty()) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED,
                        nodes = connectedNodes,
                        lastCheckTime = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                _connectionState.update {
                    it.copy(
                        status = ConnectionStatus.DISCONNECTED,
                        lastCheckTime = System.currentTimeMillis()
                    )
                }
            }
            delay(5000)
        }
    }

    fun logSyncEvent(event: SyncEvent) {
        _syncHistory.update { history ->
            (history + event).takeLast(50)
        }
    }
}
