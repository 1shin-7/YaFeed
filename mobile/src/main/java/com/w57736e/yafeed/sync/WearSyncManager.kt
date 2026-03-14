package com.w57736e.yafeed.sync

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class WearSyncManager(context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)

    suspend fun syncSettings(
        uiScale: Float,
        showImages: Boolean,
        updateInterval: Int,
        listViewGrid: Boolean,
        maxCacheSize: Int,
        fontSize: Float,
        browserType: String,
        browserAvailable: Boolean,
        notificationEnabled: Boolean,
        lastModified: Long
    ) {
        val request = PutDataMapRequest.create("/settings/preferences/${System.currentTimeMillis()}").apply {
            dataMap.putFloat("uiScale", uiScale)
            dataMap.putBoolean("showImages", showImages)
            dataMap.putInt("updateInterval", updateInterval)
            dataMap.putBoolean("listViewGrid", listViewGrid)
            dataMap.putInt("maxCacheSize", maxCacheSize)
            dataMap.putFloat("fontSize", fontSize)
            dataMap.putString("browserType", browserType)
            dataMap.putBoolean("browserAvailable", browserAvailable)
            dataMap.putBoolean("notificationEnabled", notificationEnabled)
            dataMap.putLong("lastModified", lastModified)
            dataMap.putString("deviceId", "mobile")
        }
        dataClient.putDataItem(request.asPutDataRequest()).await()
    }

    suspend fun syncSources(sources: List<RssSource>) {
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
            dataMap.putString("deviceId", "mobile")
        }
        dataClient.putDataItem(request.asPutDataRequest()).await()
    }
}
