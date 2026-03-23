# YaFeed Wear OS ↔ Mobile 连接层完全重构计划

## 问题分析

### 当前问题

1. **Mobile 端始终提示未连接**
   - 根本原因：使用 `CapabilityClient.getAllCapabilities()` 但没有声明任何 capabilities
   - 缺失文件：`res/values/wear.xml` 未定义 `android_wear_capabilities`

2. **Wear 端点击"请求输入"无响应**
   - MessageClient 配置不完整
   - Mobile 端 `MobileMessageListenerService` 缺少 `CAPABILITY_CHANGED` intent-filter

3. **Wear 端点击同步源/设置触发崩溃**
   - 崩溃原因：`java.lang.IllegalArgumentException: Buffer is closed.`
   - DataClient 操作中 DataHolder 生命周期管理不当
   - 缺乏完善的错误处理和重试机制

4. **AndroidManifest 权限缺失**
   - 缺少 `BLUETOOTH` 和 `BLUETOOTH_CONNECT` 权限（Android 12+ 需要）
   - 缺少 Capability 声明

### 架构问题

- **多个独立的连接管理器**：`WearConnectionManager`、`WearConnectionStateManager`、`MobileSyncManager`、`WearSyncManager`、`WearMessageHandler`
- **缺乏统一的状态管理**：连接状态分散在多个类中
- **错误处理不完善**：异常直接抛出导致应用崩溃
- **缺乏重试机制**：DataClient 操作失败后没有重试
- **缺乏超时控制**：可能导致无限等待

---

## 重构目标

1. **统一连接管理架构**：单一职责、清晰的层次结构
2. **完善错误处理**：优雅降级、用户友好的错误提示
3. **添加重试机制**：指数退避重试
4. **修复 AndroidManifest**：添加所有必要的权限和 Capability 声明
5. **修复 DataClient 生命周期**：正确管理 DataHolder 生命周期
6. **添加超时控制**：防止无限等待

---

## 新架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────────────┐
│                      WearableConnectionManager               │
│  (统一连接管理 - CapabilityClient + NodeClient)              │
├─────────────────────────────────────────────────────────────┤
│                      WearableDataSyncManager                 │
│  (数据同步 - DataClient + AssetClient)                       │
├─────────────────────────────────────────────────────────────┤
│                      WearableMessageManager                  │
│  (消息通信 - MessageClient)                                   │
├─────────────────────────────────────────────────────────────┤
│                      WearableListenerService                 │
│  (全局监听 - DataEvent + MessageEvent + CapabilityEvent)     │
└─────────────────────────────────────────────────────────────┘
```

### 数据流

```
Mobile App
    │
    ├─ WearableConnectionManager ──┐
    │   - checkConnection()        │
    │   - getConnectedNodes()      │
    │   - observeConnectionState() │
    │                              │
    ├─ WearableDataSyncManager ────┼──► Wear OS App
    │   - syncSettings()           │
    │   - syncSources()            │
    │   - handleDataChanged()      │
    │                              │
    ├─ WearableMessageManager ─────┘
    │   - sendOpenPage()
    │   - requestInput()
    │   - handleMessage()
    │
    └─ WearableListenerService
        - onDataChanged()
        - onMessageReceived()
        - onCapabilityChanged()
```

---

## 文件变更清单

### 1. AndroidManifest.xml (Mobile)

```xml
<!-- 添加必要的权限 -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- 添加 WearableListenerService 的完整 intent-filter -->
<service android:name=".sync.WearableListenerService" android:exported="true">
    <intent-filter>
        <action android:name="com.google.android.gms.wearable.DATA_CHANGED" />
        <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
        <action android:name="com.google.android.gms.wearable.CAPABILITY_CHANGED" />
    </intent-filter>
</service>
```

### 2. AndroidManifest.xml (Wear)

```xml
<!-- 添加必要的权限 -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- 添加 WearableListenerService 的完整 intent-filter -->
<service android:name=".sync.WearableListenerService" android:exported="true">
    <intent-filter>
        <action android:name="com.google.android.gms.wearable.DATA_CHANGED" />
        <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
        <action android:name="com.google.android.gms.wearable.CAPABILITY_CHANGED" />
    </intent-filter>
</service>
```

### 3. 新增文件

#### mobile/src/main/res/values/wear.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array name="android_wear_capabilities" translatable="false">
        <item>yafeed_mobile</item>
    </string-array>
</resources>
```

#### wear/src/main/res/values/wear.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array name="android_wear_capabilities" translatable="false">
        <item>yafeed_wear</item>
    </string-array>
</resources>
```

### 4. 重构文件

#### mobile/src/main/java/com/w57736e/yafeed/sync/WearableConnectionManager.kt
```kotlin
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
 */
class WearableConnectionManager(private val context: Context) {
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)
    private val nodeClient: NodeClient = Wearable.getNodeClient(context)
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
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
}
```

#### wear/src/main/java/com/w57736e/yafeed/sync/WearableConnectionManager.kt
```kotlin
// 与 Mobile 端相同的实现，但使用 WEAR_CAPABILITY
```

#### mobile/src/main/java/com/w57736e/yafeed/sync/WearableDataSyncManager.kt
```kotlin
package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

/**
 * 统一的数据同步管理器
 * 负责通过 DataClient 同步数据
 */
class WearableDataSyncManager(private val context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    
    companion object {
        private const val TAG = "WearableDataSync"
        private const val SETTINGS_PATH = "/yafeed/settings"
        private const val SOURCES_PATH = "/yafeed/sources"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY = 1_000L
        private const val TIMEOUT_MS = 10_000L
    }
    
    /**
     * 同步设置到 Wear OS
     * 使用原子操作和重试机制
     */
    suspend fun syncSettings(bundle: SettingsBundle): Result<Unit> {
        return try {
            val request = PutDataMapRequest.create(SETTINGS_PATH).apply {
                dataMap.apply {
                    putFloat("uiScale", bundle.uiScale)
                    putBoolean("showImages", bundle.showImages)
                    putInt("updateInterval", bundle.updateInterval)
                    putBoolean("listViewGrid", bundle.listViewGrid)
                    putInt("maxCacheSize", bundle.maxCacheSize)
                    putFloat("fontSize", bundle.fontSize)
                    putString("browserType", bundle.browserType)
                    putBoolean("browserAvailable", bundle.browserAvailable)
                    putBoolean("notificationEnabled", bundle.notificationEnabled)
                    putLong("lastModified", bundle.lastModified)
                    putString("deviceId", "mobile")
                }
            }
            
            // 使用 withTimeout 防止无限等待
            withTimeout(TIMEOUT_MS) {
                retryWithBackoff {
                    dataClient.putDataItem(request.asPutDataRequest()).await()
                }
            }
            
            Log.d(TAG, "Settings synced successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync settings", e)
            Result.failure(e)
        }
    }
    
    /**
     * 同步 RSS 源到 Wear OS
     */
    suspend fun syncSources(sources: List<RssSource>): Result<Unit> {
        return try {
            val sourcesJson = JSONArray().apply {
                sources.forEach { source ->
                    put(JSONObject().apply {
                        put("id", source.id)
                        put("name", source.name)
                        put("url", source.url)
                        put("faviconUrl", source.faviconUrl ?: "")
                        put("notificationEnabled", source.notificationEnabled)
                        put("order", source.order)
                        put("lastModified", source.lastModified)
                    })
                }
            }
            
            val request = PutDataMapRequest.create(SOURCES_PATH).apply {
                dataMap.apply {
                    putString("sources", sourcesJson.toString())
                    putLong("lastModified", System.currentTimeMillis())
                    putString("deviceId", "mobile")
                }
            }
            
            withTimeout(TIMEOUT_MS) {
                retryWithBackoff {
                    dataClient.putDataItem(request.asPutDataRequest()).await()
                }
            }
            
            Log.d(TAG, "Sources synced successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync sources", e)
            Result.failure(e)
        }
    }
    
    /**
     * 指数退避重试
     */
    private suspend fun <T> retryWithBackoff(
        maxRetries: Int = MAX_RETRIES,
        initialDelay: Long = RETRY_DELAY,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e
                Log.w(TAG, "Retry attempt ${attempt + 1}/$maxRetries after ${currentDelay}ms")
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        throw IllegalStateException("Retry failed")
    }
}
```

#### mobile/src/main/java/com/w57736e/yafeed/sync/WearableMessageManager.kt
```kotlin
package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * 统一的消息管理器
 * 负责通过 MessageClient 发送消息
 */
class WearableMessageManager(private val context: Context) {
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    
    companion object {
        private const val TAG = "WearableMessage"
        const val OPEN_PAGE_PATH = "/yafeed/open_page"
        const val REQUEST_INPUT_PATH = "/yafeed/request_input"
        const val ACK_PATH = "/yafeed/ack"
    }
    
    /**
     * 在手机上打开页面
     */
    suspend fun sendOpenPage(url: String): Result<Unit> {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                return Result.failure(Exception("No connected devices"))
            }
            
            nodes.forEach { node ->
                messageClient.sendMessage(
                    node.id,
                    OPEN_PAGE_PATH,
                    url.toByteArray()
                ).await()
            }
            
            Log.d(TAG, "Open page request sent: $url")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send open page request", e)
            Result.failure(e)
        }
    }
    
    /**
     * 请求文本输入
     */
    suspend fun requestInput(prompt: String): Result<Unit> {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            val node = nodes.firstOrNull() 
                ?: return Result.failure(Exception("No connected devices"))
            
            messageClient.sendMessage(
                node.id,
                REQUEST_INPUT_PATH,
                prompt.toByteArray()
            ).await()
            
            Log.d(TAG, "Input request sent: $prompt")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send input request", e)
            Result.failure(e)
        }
    }
    
    /**
     * 发送确认消息
     */
    suspend fun sendAck(nodeId: String, message: String): Result<Unit> {
        return try {
            messageClient.sendMessage(
                nodeId,
                ACK_PATH,
                message.toByteArray()
            ).await()
            
            Log.d(TAG, "Ack sent to $nodeId: $message")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send ack", e)
            Result.failure(e)
        }
    }
}
```

#### mobile/src/main/java/com/w57736e/yafeed/sync/WearableListenerService.kt
```kotlin
package com.w57736e.yafeed.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.android.gms.wearable.*
import com.w57736e.yafeed.R
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssSourceRepository
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * 统一的 Wearable 监听服务
 * 处理所有来自 Wear OS 的事件
 */
class WearableListenerService : com.google.android.gms.wearable.WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "WearableListener"
        private const val SETTINGS_PATH = "/yafeed/settings"
        private const val SOURCES_PATH = "/yafeed/sources"
        private const val OPEN_PAGE_PATH = "/yafeed/open_page"
        private const val REQUEST_INPUT_PATH = "/yafeed/request_input"
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "Data changed: ${dataEvents.count} events")
        
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                Log.d(TAG, "Data event path: $path")
                
                when {
                    path == SETTINGS_PATH -> handleSettingsSync(event)
                    path == SOURCES_PATH -> handleSourcesSync(event)
                }
            }
        }
    }
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Message received: ${messageEvent.path}")
        
        when (messageEvent.path) {
            OPEN_PAGE_PATH -> {
                val url = String(messageEvent.data)
                handleOpenPage(url)
            }
            REQUEST_INPUT_PATH -> {
                val prompt = String(messageEvent.data)
                handleRequestInput(prompt, messageEvent.sourceNodeId)
            }
        }
    }
    
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "Capability changed: ${capabilityInfo.name}")
        // 可以在这里更新连接状态
    }
    
    private fun handleSettingsSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val deviceId = dataMap.getString("deviceId", "")
                
                if (deviceId == "wear") {
                    val preferenceManager = PreferenceManager(applicationContext)
                    val remoteTimestamp = dataMap.getLong("lastModified", 0)
                    val localTimestamp = preferenceManager.lastModified.first()
                    
                    if (remoteTimestamp > localTimestamp) {
                        // 更新本地设置
                        preferenceManager.setUiScale(dataMap.getFloat("uiScale", 1.0f))
                        preferenceManager.setShowImages(dataMap.getBoolean("showImages", true))
                        preferenceManager.setUpdateInterval(dataMap.getInt("updateInterval", 30))
                        preferenceManager.setListViewGrid(dataMap.getBoolean("listViewGrid", false))
                        preferenceManager.setMaxCacheSize(dataMap.getInt("maxCacheSize", 20))
                        preferenceManager.setFontSize(dataMap.getFloat("fontSize", 14.0f))
                        preferenceManager.setBrowserType(dataMap.getString("browserType", "default") ?: "default")
                        preferenceManager.setBrowserAvailable(dataMap.getBoolean("browserAvailable", false))
                        preferenceManager.setNotificationEnabled(dataMap.getBoolean("notificationEnabled", false))
                        
                        Log.d(TAG, "Settings synced from wear")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle settings sync", e)
            }
        }
    }
    
    private fun handleSourcesSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val deviceId = dataMap.getString("deviceId", "")
                
                if (deviceId == "wear") {
                    val sourcesJson = dataMap.getString("sources", "[]")
                    val jsonArray = JSONArray(sourcesJson)
                    val sources = mutableListOf<RssSource>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        sources.add(
                            RssSource(
                                id = obj.getInt("id"),
                                name = obj.getString("name"),
                                url = obj.getString("url"),
                                faviconUrl = obj.getString("faviconUrl").takeIf { it.isNotBlank() },
                                notificationEnabled = obj.getBoolean("notificationEnabled"),
                                order = obj.getInt("order"),
                                lastModified = obj.getLong("lastModified")
                            )
                        )
                    }
                    
                    val database = AppDatabase.getDatabase(applicationContext)
                    database.sourceDao().insertAll(sources)
                    
                    Log.d(TAG, "Sources synced from wear: ${sources.size} items")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle sources sync", e)
            }
        }
    }
    
    private fun handleOpenPage(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Log.d(TAG, "Opened page: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open page: $url", e)
        }
    }
    
    private fun handleRequestInput(prompt: String, nodeId: String) {
        try {
            val channelId = "wear_input"
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            val channel = NotificationChannel(
                channelId,
                "Wear Input Requests",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            
            val remoteInput = RemoteInput.Builder("input_text")
                .setLabel(prompt)
                .build()
            
            val intent = Intent(this, WearableListenerService::class.java).apply {
                action = "REPLY_ACTION"
                putExtra("node_id", nodeId)
            }
            
            val pendingIntent = PendingIntent.getService(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            
            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Input from Watch")
                .setContentText(prompt)
                .addAction(
                    NotificationCompat.Action.Builder(
                        0, "Reply", pendingIntent
                    ).addRemoteInput(remoteInput).build()
                )
                .build()
            
            notificationManager.notify(1, notification)
            Log.d(TAG, "Input notification shown: $prompt")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show input notification", e)
        }
    }
}
```

---

## 实施步骤

### 阶段 1: 基础设施 (预计 2 小时)

1. **创建 Capability 声明文件**
   - `mobile/src/main/res/values/wear.xml`
   - `wear/src/main/res/values/wear.xml`

2. **更新 AndroidManifest.xml**
   - 添加 BLUETOOTH 权限
   - 添加 BLUETOOTH_CONNECT 权限
   - 添加 ACCESS_FINE_LOCATION 权限
   - 更新 WearableListenerService 的 intent-filter

### 阶段 2: 核心组件重构 (预计 4 小时)

3. **创建 WearableConnectionManager**
   - 统一的连接管理
   - Capability-based 设备发现
   - 连接状态监控

4. **创建 WearableDataSyncManager**
   - 统一的数据同步
   - 重试机制
   - 超时控制
   - 错误处理

5. **创建 WearableMessageManager**
   - 统一的消息通信
   - 错误处理

6. **重构 WearableListenerService**
   - 统一处理所有事件
   - 完善错误处理
   - 添加日志

### 阶段 3: 集成和测试 (预计 2 小时)

7. **更新 MainActivity**
   - 使用新的 WearableConnectionManager
   - 更新连接状态监听

8. **更新 ViewModel**
   - 使用新的同步管理器
   - 添加错误处理
   - 显示用户友好的错误提示

9. **测试**
   - 连接测试
   - 同步测试
   - 消息测试
   - 崩溃测试

---

## 测试计划

### 连接测试

1. **基础连接**
   - [ ] Mobile 端能够发现 Wear 设备
   - [ ] Wear 端能够发现 Mobile 设备
   - [ ] 连接状态正确显示

2. **断开重连**
   - [ ] 断开后状态正确更新
   - [ ] 重连后状态正确恢复

### 同步测试

1. **设置同步**
   - [ ] Mobile → Wear 同步成功
   - [ ] Wear → Mobile 同步成功
   - [ ] 冲突解决正确

2. **源同步**
   - [ ] Mobile → Wear 同步成功
   - [ ] Wear → Mobile 同步成功
   - [ ] 大量数据同步稳定

### 消息测试

1. **打开页面**
   - [ ] Wear 端点击"打开页面"成功
   - [ ] Mobile 端正确接收并打开 URL

2. **请求输入**
   - [ ] Wear 端点击"请求输入"成功
   - [ ] Mobile 竾示通知
   - [ ] 输入后正确返回

### 稳定性测试

1. **崩溃测试**
   - [ ] 同步源不崩溃
   - [ ] 同步设置不崩溃
   - [ ] 断网情况优雅降级

2. **压力测试**
   - [ ] 连续同步 100 次
   - [ ] 快速切换连接状态
   - [ ] 内存泄漏检查

---

## 风险和缓解措施

### 风险 1: Android 12+ 权限问题

**缓解措施**:
- 在 AndroidManifest 中声明所有必要权限
- 添加运行时权限请求（如果需要）
- 测试不同 Android 版本

### 风险 2: DataClient 仍然可能崩溃

**缓解措施**:
- 添加 try-catch 包裹所有 DataClient 操作
- 使用 Result 类型返回操作结果
- 添加重试机制
- 添加超时控制

### 风险 3: 连接不稳定

**缓解措施**:
- 使用 CapabilityClient 进行设备发现
- 添加重连机制
- 添加连接状态监控
- 提供手动刷新选项

---

## 后续优化

1. **添加连接质量指示器**
   - 显示信号强度
   - 显示延迟

2. **添加同步进度条**
   - 显示同步进度
   - 显示剩余时间

3. **添加离线支持**
   - 本地缓存
   - 离线队列
   - 自动重试

4. **添加性能监控**
   - 同步耗时统计
   - 错误率统计
   - 连接稳定性统计
