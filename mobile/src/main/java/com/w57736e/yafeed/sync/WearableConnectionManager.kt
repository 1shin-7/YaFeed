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
import kotlinx.coroutines.tasks.await

/**
 * 统一的 Wearable 连接管理器
 * 负责设备发现、连接状态监控
 * 替代原来的 WearConnectionManager 和 WearConnectionStateManager
 */
class WearableConnectionManager(private val context: Context) {
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)
    private val nodeClient: NodeClient = Wearable.getNodeClient(context)
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _syncHistory = MutableStateFlow<List<SyncEvent>>(emptyList())
    val syncHistory: StateFlow<List<SyncEvent>> = _syncHistory.asStateFlow()
    
    companion object {
        private const val TAG = "WearableConnection"
        private const val MOBILE_CAPABILITY = "yafeed_mobile"
        private const val WEAR_CAPABILITY = "yafeed_wear"
        private const val CHECK_INTERVAL = 10_000L
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY = 1_000L
    }
    
    /**
     * 检查连接状态
     * 使用 CapabilityClient 进行设备发现
     */
    suspend fun checkConnection(): Boolean {
        return try {
            // 检查 Mobile 设备能力
            val mobileCapabilities = capabilityClient
                .getCapability(MOBILE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
            
            // 检查 Wear 设备能力
            val wearCapabilities = capabilityClient
                .getCapability(WEAR_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
            
            val allNodes = (mobileCapabilities.nodes + wearCapabilities.nodes).distinctBy { it.id }
            
            val connectedNodes = allNodes.map { node ->
                ConnectedNode(
                    id = node.id,
                    displayName = node.displayName,
                    isNearby = node.isNearby
                )
            }
            
            _connectionState.value = ConnectionState(
                status = if (connectedNodes.isNotEmpty()) ConnectionStatus.CONNECTED 
                         else ConnectionStatus.DISCONNECTED,
                nodes = connectedNodes,
                lastCheckTime = System.currentTimeMillis()
            )
            _isConnected.value = connectedNodes.isNotEmpty()
            
            Log.d(TAG, "Connection check: ${connectedNodes.size} nodes found")
            connectedNodes.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Connection check failed", e)
            _connectionState.value = ConnectionState(
                status = ConnectionStatus.DISCONNECTED,
                lastCheckTime = System.currentTimeMillis()
            )
            _isConnected.value = false
            false
        }
    }
    
    /**
     * 获取已连接的节点
     */
    suspend fun getConnectedNodes(): List<ConnectedNode> {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            nodes.map { node ->
                ConnectedNode(
                    id = node.id,
                    displayName = node.displayName,
                    isNearby = node.isNearby
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get connected nodes", e)
            emptyList()
        }
    }
    
    /**
     * 启动连接监控
     */
    suspend fun startMonitoring() {
        while (true) {
            checkConnection()
            delay(CHECK_INTERVAL)
        }
    }
    
    /**
     * 带重试的连接检查
     */
    suspend fun checkConnectionWithRetry(): Boolean {
        repeat(MAX_RETRIES) { attempt ->
            val result = checkConnection()
            if (result) return true
            if (attempt < MAX_RETRIES - 1) {
                delay(RETRY_DELAY * (attempt + 1))
            }
        }
        return false
    }
    
    /**
     * 记录同步事件
     */
    fun logSyncEvent(event: SyncEvent) {
        _syncHistory.value = (_syncHistory.value + event).takeLast(50)
    }
}
