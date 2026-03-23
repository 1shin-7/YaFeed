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
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Mobile 端的 Wearable 监听服务 - 处理来自 Wear 的数据和消息
 */
class WearableListenerService : com.google.android.gms.wearable.WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "LISTENER_MOBILE"
        private const val OPEN_PAGE_PATH = "/yafeed/open_page"
        private const val REQUEST_INPUT_PATH = "/yafeed/request_input"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== Service onCreate ===")
    }
    
    override fun onDestroy() {
        Log.d(TAG, "=== Service onDestroy ===")
        super.onDestroy()
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, ">>> onDataChanged: ${dataEvents.count} events")
        
        try {
            dataEvents.forEach { event ->
                val uri = event.dataItem.uri
                val path = uri.path
                Log.d(TAG, "    event type=${event.type}, uri=$uri, path=$path")
                
                if (event.type == DataEvent.TYPE_CHANGED) {
                    when {
                        path?.startsWith("/yafeed/settings") == true -> {
                            Log.d(TAG, "    -> handling settings sync")
                            handleSettingsSync(event)
                        }
                        path?.startsWith("/yafeed/sources") == true -> {
                            Log.d(TAG, "    -> handling sources sync")
                            handleSourcesSync(event)
                        }
                        else -> {
                            Log.w(TAG, "    -> unknown path: $path")
                        }
                    }
                } else if (event.type == DataEvent.TYPE_DELETED) {
                    Log.d(TAG, "    -> data deleted: $uri")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "    onDataChanged error: ${e.message}", e)
        }
        
        Log.d(TAG, "<<< onDataChanged complete")
    }
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, ">>> onMessageReceived")
        Log.d(TAG, "    path: ${messageEvent.path}")
        Log.d(TAG, "    sourceNodeId: ${messageEvent.sourceNodeId}")
        Log.d(TAG, "    data size: ${messageEvent.data?.size ?: 0}")
        
        try {
            when (messageEvent.path) {
                OPEN_PAGE_PATH -> {
                    val url = String(messageEvent.data)
                    Log.d(TAG, "    -> open page: $url")
                    handleOpenPage(url)
                }
                REQUEST_INPUT_PATH -> {
                    val prompt = String(messageEvent.data)
                    Log.d(TAG, "    -> request input: $prompt")
                    handleRequestInput(prompt, messageEvent.sourceNodeId)
                }
                else -> {
                    Log.w(TAG, "    -> unknown message path: ${messageEvent.path}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "    onMessageReceived error: ${e.message}", e)
        }
        
        Log.d(TAG, "<<< onMessageReceived complete")
    }
    
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ${capabilityInfo.name}, nodes=${capabilityInfo.nodes.size}")
        capabilityInfo.nodes.forEach { node ->
            Log.d(TAG, "    node: id=${node.id}, displayName=${node.displayName}, isNearby=${node.isNearby}")
        }
    }
    
    private fun handleSettingsSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val deviceId = dataMap.getString(SyncKeys.DEVICE_ID, "")
                val timestamp = dataMap.getLong(SyncKeys.SYNC_TIMESTAMP, 0)
                
                Log.d(TAG, "    settings: deviceId=$deviceId, timestamp=$timestamp")
                
                if (deviceId == SyncKeys.DEVICE_WEAR) {
                    val preferenceManager = PreferenceManager(applicationContext)
                    val remoteTimestamp = dataMap.getLong(SyncKeys.LAST_MODIFIED, 0)
                    val localTimestamp = preferenceManager.lastModified.first()
                    
                    Log.d(TAG, "    settings: remoteTs=$remoteTimestamp, localTs=$localTimestamp")
                    
                    if (remoteTimestamp > localTimestamp) {
                        // 读取所有设置
                        val showImages = dataMap.getBoolean(SyncKeys.SHOW_IMAGES, true)
                        val updateInterval = dataMap.getLong(SyncKeys.UPDATE_INTERVAL, 30L)
                        val listViewGrid = dataMap.getBoolean(SyncKeys.LIST_VIEW_GRID, false)
                        val maxCacheSize = dataMap.getInt(SyncKeys.MAX_CACHE_SIZE, 20)
                        val fontSize = dataMap.getFloat(SyncKeys.FONT_SIZE, 14f)
                        val browserType = dataMap.getString(SyncKeys.BROWSER_TYPE, "default") ?: "default"
                        val browserAvailable = dataMap.getBoolean(SyncKeys.BROWSER_AVAILABLE, false)
                        val notificationEnabled = dataMap.getBoolean(SyncKeys.NOTIFICATION_ENABLED, false)
                        val saveImagesOnFavorite = dataMap.getBoolean(SyncKeys.SAVE_IMAGES_ON_FAVORITE, false)
                        val useOriginalImagePreview = dataMap.getBoolean(SyncKeys.USE_ORIGINAL_IMAGE_PREVIEW, false)
                        
                        Log.d(TAG, "    settings: applying showImages=$showImages, updateInterval=$updateInterval")
                        
                        // 写入设置
                        preferenceManager.setShowImages(showImages)
                        preferenceManager.setUpdateInterval(updateInterval)
                        preferenceManager.setListViewGrid(listViewGrid)
                        preferenceManager.setMaxCacheSize(maxCacheSize)
                        preferenceManager.setFontSize(fontSize)
                        preferenceManager.setBrowserType(browserType)
                        preferenceManager.setBrowserAvailable(browserAvailable)
                        preferenceManager.setNotificationEnabled(notificationEnabled)
                        preferenceManager.setSaveImagesOnFavorite(saveImagesOnFavorite)
                        preferenceManager.setUseOriginalImagePreview(useOriginalImagePreview)
                        
                        Log.d(TAG, "    settings: SUCCESS - applied all settings")
                    } else {
                        Log.d(TAG, "    settings: skipping - local is newer")
                    }
                } else {
                    Log.d(TAG, "    settings: skipping - deviceId=$deviceId != wear")
                }
            } catch (e: Exception) {
                Log.e(TAG, "    settings: FAILED - ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }
    }
    
    private fun handleSourcesSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val deviceId = dataMap.getString(SyncKeys.DEVICE_ID, "")
                val timestamp = dataMap.getLong(SyncKeys.SYNC_TIMESTAMP, 0)
                val count = dataMap.getInt(SyncKeys.SOURCES_COUNT, 0)
                
                Log.d(TAG, "    sources: deviceId=$deviceId, timestamp=$timestamp, count=$count")
                
                if (deviceId == SyncKeys.DEVICE_WEAR) {
                    val sourcesDataMaps = dataMap.getDataMapArrayList(SyncKeys.SOURCES_LIST)
                    
                    if (sourcesDataMaps == null) {
                        Log.e(TAG, "    sources: sourcesDataMaps is null!")
                        return@launch
                    }
                    
                    Log.d(TAG, "    sources: received ${sourcesDataMaps.size} items")
                    
                    val sources = sourcesDataMaps.mapIndexed { index, sourceMap ->
                        val source = RssSource(
                            id = sourceMap.getInt(SyncKeys.SOURCE_ID),
                            name = sourceMap.getString(SyncKeys.SOURCE_NAME) ?: "",
                            url = sourceMap.getString(SyncKeys.SOURCE_URL) ?: "",
                            faviconUrl = sourceMap.getString(SyncKeys.SOURCE_FAVICON).takeIf { !it.isNullOrBlank() },
                            notificationEnabled = sourceMap.getBoolean(SyncKeys.SOURCE_NOTIFICATION),
                            order = sourceMap.getInt(SyncKeys.SOURCE_ORDER),
                            lastModified = sourceMap.getLong(SyncKeys.SOURCE_LAST_MODIFIED)
                        )
                        Log.d(TAG, "    sources: [$index] id=${source.id}, name=${source.name}")
                        source
                    }
                    
                    val database = AppDatabase.getDatabase(applicationContext)
                    database.sourceDao().insertAll(sources)
                    
                    Log.d(TAG, "    sources: SUCCESS - synced ${sources.size} sources")
                } else {
                    Log.d(TAG, "    sources: skipping - deviceId=$deviceId != wear")
                }
            } catch (e: Exception) {
                Log.e(TAG, "    sources: FAILED - ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }
    }
    
    private fun handleOpenPage(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Log.d(TAG, "    open page: SUCCESS - $url")
        } catch (e: Exception) {
            Log.e(TAG, "    open page: FAILED - ${e.message}", e)
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
            Log.d(TAG, "    request input: SUCCESS - notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "    request input: FAILED - ${e.message}", e)
        }
    }
}
