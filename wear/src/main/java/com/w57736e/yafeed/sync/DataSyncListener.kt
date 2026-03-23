package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
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
 * 数据同步监听器 - 本地监听 DataClient 事件
 */
class DataSyncListener(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : DataClient.OnDataChangedListener {
    
    companion object {
        private const val TAG = "DATA_SYNC_LISTENER"
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
                        path?.startsWith("/yafeed/sync_request") == true -> {
                            Log.d(TAG, "    -> handling sync request from Mobile")
                            handleSyncRequest()
                        }
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
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "    onDataChanged error: ${e.message}", e)
        }
        
        Log.d(TAG, "<<< onDataChanged complete")
    }
    
    private fun handleSyncRequest() {
        scope.launch {
            try {
                Log.d(TAG, "    sync request: syncing sources to Mobile")
                val database = AppDatabase.getDatabase(context)
                val repository = RssRepository(
                    database.sourceDao(),
                    database.articleDao(),
                    database.favoriteDao(),
                    com.prof18.rssparser.RssParser()
                )
                val sources = repository.getAllSources().first()
                Log.d(TAG, "    sync request: found ${sources.size} sources")
                
                if (sources.isNotEmpty()) {
                    val connectionManager = WearableConnectionManager(context)
                    val syncManager = WearableDataSyncManager(context, connectionManager)
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
                
                Log.d(TAG, "    settings: deviceId=$deviceId")
                
                if (deviceId == SyncKeys.DEVICE_MOBILE) {
                    val preferenceManager = PreferenceManager(context)
                    val remoteTimestamp = dataMap.getLong(SyncKeys.LAST_MODIFIED, 0)
                    val localTimestamp = preferenceManager.lastModified.first()
                    
                    Log.d(TAG, "    settings: remoteTs=$remoteTimestamp, localTs=$localTimestamp")
                    
                    if (remoteTimestamp < localTimestamp) {
                        Log.d(TAG, "    settings: skipping - local is newer")
                        return@launch
                    }
                    
                    preferenceManager.setShowImages(dataMap.getBoolean(SyncKeys.SHOW_IMAGES, true))
                    preferenceManager.setUpdateInterval(dataMap.getLong(SyncKeys.UPDATE_INTERVAL, 30L))
                    preferenceManager.setListViewGrid(dataMap.getBoolean(SyncKeys.LIST_VIEW_GRID, false))
                    preferenceManager.setMaxCacheSize(dataMap.getInt(SyncKeys.MAX_CACHE_SIZE, 20))
                    preferenceManager.setFontSize(dataMap.getFloat(SyncKeys.FONT_SIZE, 14f))
                    preferenceManager.setBrowserType(dataMap.getString(SyncKeys.BROWSER_TYPE, "default") ?: "default")
                    preferenceManager.setBrowserAvailable(dataMap.getBoolean(SyncKeys.BROWSER_AVAILABLE, false))
                    preferenceManager.setNotificationEnabled(dataMap.getBoolean(SyncKeys.NOTIFICATION_ENABLED, false))
                    preferenceManager.setSaveImagesOnFavorite(dataMap.getBoolean(SyncKeys.SAVE_IMAGES_ON_FAVORITE, false))
                    preferenceManager.setUseOriginalImagePreview(dataMap.getBoolean(SyncKeys.USE_ORIGINAL_IMAGE_PREVIEW, false))
                    
                    Log.d(TAG, "    settings: SUCCESS")
                }
            } catch (e: Exception) {
                Log.e(TAG, "    settings: FAILED - ${e.message}", e)
            }
        }
    }
    
    private fun handleSourcesSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val deviceId = dataMap.getString(SyncKeys.DEVICE_ID, "")
                
                Log.d(TAG, "    sources: deviceId=$deviceId")
                
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
                    
                    val database = AppDatabase.getDatabase(context)
                    val repository = RssRepository(
                        database.sourceDao(),
                        database.articleDao(),
                        database.favoriteDao(),
                        com.prof18.rssparser.RssParser()
                    )
                    
                    Log.d(TAG, "    sources: calling repository.syncSources(${sources.size})")
                    repository.syncSources(sources)
                    Log.d(TAG, "    sources: SUCCESS - synced ${sources.size} sources")
                }
            } catch (e: Exception) {
                Log.e(TAG, "    sources: FAILED - ${e.message}", e)
            }
        }
    }
}
