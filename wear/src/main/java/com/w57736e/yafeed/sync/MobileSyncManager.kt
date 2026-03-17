package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class MobileSyncManager(
    context: Context,
    private val connectionManager: WearConnectionStateManager
) {
    private val dataClient: DataClient = Wearable.getDataClient(context)

    suspend fun syncSettingsAtomic(bundle: SettingsBundle) {
        connectionManager.logSyncEvent(
            SyncEvent(
                timestamp = System.currentTimeMillis(),
                type = SyncType.SETTINGS,
                status = SyncStatus.IN_PROGRESS
            )
        )
        try {
            val request = PutDataMapRequest.create("/settings/preferences/${System.currentTimeMillis()}").apply {
                dataMap.putFloat("uiScale", bundle.uiScale)
                dataMap.putBoolean("showImages", bundle.showImages)
                dataMap.putInt("updateInterval", bundle.updateInterval)
                dataMap.putBoolean("listViewGrid", bundle.listViewGrid)
                dataMap.putInt("maxCacheSize", bundle.maxCacheSize)
                dataMap.putFloat("fontSize", bundle.fontSize)
                dataMap.putString("browserType", bundle.browserType)
                dataMap.putBoolean("browserAvailable", bundle.browserAvailable)
                dataMap.putBoolean("notificationEnabled", bundle.notificationEnabled)
                dataMap.putLong("lastModified", bundle.lastModified)
                dataMap.putString("checksum", bundle.checksum)
                dataMap.putString("deviceId", "wear")
            }
            dataClient.putDataItem(request.asPutDataRequest()).await()
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SETTINGS,
                    status = SyncStatus.SUCCESS
                )
            )
        } catch (e: Exception) {
            Log.e("MobileSyncManager", "Settings sync failed", e)
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SETTINGS,
                    status = SyncStatus.FAILURE,
                    message = e.message ?: "Unknown error"
                )
            )
            throw e
        }
    }

    suspend fun syncSources(sources: List<RssSource>) {
        connectionManager.logSyncEvent(
            SyncEvent(
                timestamp = System.currentTimeMillis(),
                type = SyncType.SOURCES,
                status = SyncStatus.IN_PROGRESS
            )
        )
        try {
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

            val request = PutDataMapRequest.create("/rss/sources/${System.currentTimeMillis()}").apply {
                dataMap.putString("sources", sourcesJson.toString())
                dataMap.putLong("lastModified", System.currentTimeMillis())
                dataMap.putString("deviceId", "wear")
            }
            dataClient.putDataItem(request.asPutDataRequest()).await()
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SOURCES,
                    status = SyncStatus.SUCCESS
                )
            )
        } catch (e: Exception) {
            Log.e("MobileSyncManager", "Sources sync failed", e)
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SOURCES,
                    status = SyncStatus.FAILURE,
                    message = e.message ?: "Unknown error"
                )
            )
            throw e
        }
    }
}
