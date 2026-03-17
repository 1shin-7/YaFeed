package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

enum class ConnectionStatus {
    DISCONNECTED, SEARCHING, CONNECTED
}

data class ConnectedNode(
    val id: String,
    val displayName: String,
    val isNearby: Boolean
)

data class ConnectionState(
    val status: ConnectionStatus,
    val nodes: List<ConnectedNode>,
    val lastCheckTime: Long
)

data class SyncEvent(
    val timestamp: Long,
    val type: String,
    val success: Boolean,
    val message: String
)

class WearConnectionManager(context: Context) {
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)
    private val nodeClient: NodeClient = Wearable.getNodeClient(context)
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    private val maxRetries = 5

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

    suspend fun checkConnection() {
        var delay = 1000L

        repeat(maxRetries) { attempt ->
            try {
                val nodes = capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE)
                    .await()
                    .flatMap { it.value.nodes }

                if (nodes.isNotEmpty()) {
                    _isConnected.value = true
                    Log.d("WearConnection", "Connected to ${nodes.size} device(s)")
                    return
                }
            } catch (e: Exception) {
                Log.e("WearConnection", "Connection check failed (attempt ${attempt + 1}/$maxRetries)", e)
            }

            if (attempt < maxRetries - 1) {
                delay(delay)
                delay *= 2
            }
        }

        _isConnected.value = false
        Log.w("WearConnection", "Failed to connect after $maxRetries attempts")
    }

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
                _isConnected.value = connectedNodes.isNotEmpty()
            } catch (e: Exception) {
                Log.e("WearConnection", "Monitoring failed", e)
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
