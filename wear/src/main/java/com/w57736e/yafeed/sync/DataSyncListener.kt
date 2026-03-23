package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DataSyncListener(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : DataClient.OnDataChangedListener {
    
    companion object {
        private const val TAG = "SYNC"
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            dataEvents.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val path = event.dataItem.uri.path
                    when {
                        path?.startsWith("/yafeed/sync_request") == true -> handleSyncRequest()
                        path?.startsWith("/yafeed/settings") == true -> handleSettingsSync(event)
                        path?.startsWith("/yafeed/sources") == true -> handleSourcesSync(event)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onDataChanged failed: ${e.message}")
        }
    }
    
    private fun handleSyncRequest() {
        scope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val repository = RssRepository(
                    database.sourceDao(),
                    database.articleDao(),
                    database.favoriteDao(),
                    com.prof18.rssparser.RssParser()
                )
                val sources = repository.getAllSources().first()
                if (sources.isNotEmpty()) {
                    val connectionManager = WearableConnectionManager(context)
                    val syncManager = WearableDataSyncManager(context, connectionManager)
                    syncManager.syncSources(sources)
                    Log.d(TAG, "syncRequest: synced ${sources.size} sources")
                }
            } catch (e: Exception) {
                Log.e(TAG, "syncRequest failed: ${e.message}")
            }
        }
    }
    
    private fun handleSettingsSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                if (dataMap.getString(SyncKeys.DEVICE_ID, "") != SyncKeys.DEVICE_MOBILE) return@launch
                
                val preferenceManager = PreferenceManager(context)
                val remoteTimestamp = dataMap.getLong(SyncKeys.LAST_MODIFIED, 0)
                val localTimestamp = preferenceManager.lastModified.first()
                
                if (remoteTimestamp < localTimestamp) return@launch
                
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
                
                Log.d(TAG, "settingsSync: applied from Mobile")
            } catch (e: Exception) {
                Log.e(TAG, "settingsSync failed: ${e.message}")
            }
        }
    }
    
    private fun handleSourcesSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                if (dataMap.getString(SyncKeys.DEVICE_ID, "") != SyncKeys.DEVICE_MOBILE) return@launch
                
                val sourcesDataMaps = dataMap.getDataMapArrayList(SyncKeys.SOURCES_LIST) ?: return@launch
                
                val sources = sourcesDataMaps.map { sourceMap ->
                    RssSource(
                        id = sourceMap.getInt(SyncKeys.SOURCE_ID),
                        name = sourceMap.getString(SyncKeys.SOURCE_NAME) ?: "",
                        url = sourceMap.getString(SyncKeys.SOURCE_URL) ?: "",
                        faviconUrl = sourceMap.getString(SyncKeys.SOURCE_FAVICON).takeIf { !it.isNullOrBlank() },
                        notificationEnabled = sourceMap.getBoolean(SyncKeys.SOURCE_NOTIFICATION),
                        order = sourceMap.getInt(SyncKeys.SOURCE_ORDER),
                        lastModified = sourceMap.getLong(SyncKeys.SOURCE_LAST_MODIFIED)
                    )
                }
                
                val database = AppDatabase.getDatabase(context)
                val repository = RssRepository(
                    database.sourceDao(),
                    database.articleDao(),
                    database.favoriteDao(),
                    com.prof18.rssparser.RssParser()
                )
                repository.syncSources(sources)
                Log.d(TAG, "sourcesSync: synced ${sources.size} sources")
            } catch (e: Exception) {
                Log.e(TAG, "sourcesSync failed: ${e.message}")
            }
        }
    }
}
