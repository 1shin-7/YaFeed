package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Mobile 端数据同步监听器
 * Wear 是唯一数据源，Mobile 只接收数据
 */
class DataSyncListener(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : DataClient.OnDataChangedListener {
    
    companion object {
        private const val TAG = "DATA_SYNC_MOBILE"
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
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "    onDataChanged error: ${e.message}", e)
        }
        
        Log.d(TAG, "<<< onDataChanged complete")
    }
    
    private fun handleSettingsSync(event: DataEvent) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val deviceId = dataMap.getString(SyncKeys.DEVICE_ID, "")
                
                Log.d(TAG, "    settings: deviceId=$deviceId")
                
                if (deviceId == SyncKeys.DEVICE_WEAR) {
                    val preferenceManager = PreferenceManager(context)
                    
                    // Always accept Wear settings (Wear is source of truth)
                    preferenceManager.setUiScale(dataMap.getFloat(SyncKeys.UI_SCALE, 1.0f))
                    preferenceManager.setShowImages(dataMap.getBoolean(SyncKeys.SHOW_IMAGES, true))
                    preferenceManager.setUpdateInterval(dataMap.getInt(SyncKeys.UPDATE_INTERVAL, 30))
                    preferenceManager.setListViewGrid(dataMap.getBoolean(SyncKeys.LIST_VIEW_GRID, false))
                    preferenceManager.setMaxCacheSize(dataMap.getInt(SyncKeys.MAX_CACHE_SIZE, 20))
                    preferenceManager.setFontSize(dataMap.getFloat(SyncKeys.FONT_SIZE, 14.0f))
                    preferenceManager.setBrowserType(dataMap.getString(SyncKeys.BROWSER_TYPE, "default") ?: "default")
                    preferenceManager.setBrowserAvailable(dataMap.getBoolean(SyncKeys.BROWSER_AVAILABLE, false))
                    preferenceManager.setNotificationEnabled(dataMap.getBoolean(SyncKeys.NOTIFICATION_ENABLED, false))
                    
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
                
                if (deviceId == SyncKeys.DEVICE_WEAR) {
                    val sourcesDataMaps = dataMap.getDataMapArrayList(SyncKeys.SOURCES_LIST)
                    
                    if (sourcesDataMaps == null) {
                        Log.e(TAG, "    sources: sourcesDataMaps is null!")
                        return@launch
                    }
                    
                    Log.d(TAG, "    sources: received ${sourcesDataMaps.size} items")
                    
                    val sources = sourcesDataMaps.mapIndexed { index, sourceMap ->
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
                    // Wear is source of truth: clear local and insert Wear data
                    Log.d(TAG, "    sources: clearing local and inserting ${sources.size} sources from Wear")
                    database.sourceDao().deleteAllSources()
                    database.sourceDao().insertAll(sources)
                    
                    Log.d(TAG, "    sources: SUCCESS - synced ${sources.size} sources")
                }
            } catch (e: Exception) {
                Log.e(TAG, "    sources: FAILED - ${e.message}", e)
            }
        }
    }
}
