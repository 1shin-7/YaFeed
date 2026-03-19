package com.w57736e.yafeed.sync

import android.util.Log
import com.google.android.gms.wearable.*
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Wear 端的 Wearable 监听服务 - 处理来自 Mobile 的数据
 */
class WearableListenerService : com.google.android.gms.wearable.WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "LISTENER_WEAR"
        private const val REQUEST_SYNC_PATH = "/yafeed/request_sync"
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
    
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ${capabilityInfo.name}, nodes=${capabilityInfo.nodes.size}")
    }
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, ">>> onMessageReceived: path=${messageEvent.path}, from=${messageEvent.sourceNodeId}")
        
        when (messageEvent.path) {
            REQUEST_SYNC_PATH -> {
                Log.d(TAG, "    -> handling sync request")
                handleSyncRequest()
            }
            else -> {
                Log.d(TAG, "    -> unknown message path: ${messageEvent.path}")
            }
        }
        
        Log.d(TAG, "<<< onMessageReceived complete")
    }
    
    private fun handleSyncRequest() {
        scope.launch {
            try {
                Log.d(TAG, "    sync request: syncing sources to Mobile")
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = RssRepository(
                    database.sourceDao(),
                    database.articleDao(),
                    database.favoriteDao(),
                    com.prof18.rssparser.RssParser()
                )
                val sources = repository.getAllSources().first()
                Log.d(TAG, "    sync request: found ${sources.size} sources")
                
                if (sources.isNotEmpty()) {
                    val connectionManager = WearableConnectionManager(applicationContext)
                    val syncManager = WearableDataSyncManager(applicationContext, connectionManager)
                    syncManager.syncSources(sources)
                    Log.d(TAG, "    sync request: SUCCESS")
                } else {
                    Log.d(TAG, "    sync request: no sources to sync")
                }
            } catch (e: Exception) {
                Log.e(TAG, "    sync request: FAILED - ${e.message}", e)
            }
        }
    }
    
    private fun handleSettingsSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val deviceId = dataMap.getString(SyncKeys.DEVICE_ID, "")
                val timestamp = dataMap.getLong(SyncKeys.SYNC_TIMESTAMP, 0)
                
                Log.d(TAG, "    settings: deviceId=$deviceId, timestamp=$timestamp")
                
                if (deviceId == SyncKeys.DEVICE_MOBILE) {
                    val preferenceManager = PreferenceManager(applicationContext)
                    val remoteTimestamp = dataMap.getLong(SyncKeys.LAST_MODIFIED, 0)
                    val localTimestamp = preferenceManager.lastModified.first()
                    
                    Log.d(TAG, "    settings: remoteTs=$remoteTimestamp, localTs=$localTimestamp")
                    
                    if (remoteTimestamp < localTimestamp) {
                        Log.d(TAG, "    settings: skipping - local is newer")
                        return@launch
                    }
                    
                    // 读取所有设置
                    val uiScale = dataMap.getFloat(SyncKeys.UI_SCALE, 1.0f)
                    val showImages = dataMap.getBoolean(SyncKeys.SHOW_IMAGES, true)
                    val updateInterval = dataMap.getInt(SyncKeys.UPDATE_INTERVAL, 30)
                    val listViewGrid = dataMap.getBoolean(SyncKeys.LIST_VIEW_GRID, false)
                    val maxCacheSize = dataMap.getInt(SyncKeys.MAX_CACHE_SIZE, 20)
                    val fontSize = dataMap.getFloat(SyncKeys.FONT_SIZE, 14.0f)
                    val browserType = dataMap.getString(SyncKeys.BROWSER_TYPE, "default") ?: "default"
                    val browserAvailable = dataMap.getBoolean(SyncKeys.BROWSER_AVAILABLE, false)
                    val notificationEnabled = dataMap.getBoolean(SyncKeys.NOTIFICATION_ENABLED, false)
                    
                    Log.d(TAG, "    settings: applying uiScale=$uiScale, showImages=$showImages")
                    Log.d(TAG, "    settings: applying updateInterval=$updateInterval, fontSize=$fontSize")
                    
                    // 写入设置
                    preferenceManager.setUiScale(uiScale)
                    preferenceManager.setShowImages(showImages)
                    preferenceManager.setUpdateInterval(updateInterval.toLong())
                    preferenceManager.setListViewGrid(listViewGrid)
                    preferenceManager.setMaxCacheSize(maxCacheSize)
                    preferenceManager.setFontSize(fontSize)
                    preferenceManager.setBrowserType(browserType)
                    preferenceManager.setBrowserAvailable(browserAvailable)
                    preferenceManager.setNotificationEnabled(notificationEnabled)
                    
                    Log.d(TAG, "    settings: SUCCESS - applied all settings")
                } else {
                    Log.d(TAG, "    settings: skipping - deviceId=$deviceId != mobile")
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
                
                if (deviceId == SyncKeys.DEVICE_MOBILE) {
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
                    val repository = RssRepository(
                        database.sourceDao(),
                        database.articleDao(),
                        database.favoriteDao(),
                        com.prof18.rssparser.RssParser()
                    )
                    
                    Log.d(TAG, "    sources: calling repository.syncSources(${sources.size})")
                    repository.syncSources(sources)
                    Log.d(TAG, "    sources: SUCCESS - synced ${sources.size} sources")
                } else {
                    Log.d(TAG, "    sources: skipping - deviceId=$deviceId != mobile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "    sources: FAILED - ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }
    }
}
