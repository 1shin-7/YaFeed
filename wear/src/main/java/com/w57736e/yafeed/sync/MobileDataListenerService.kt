package com.w57736e.yafeed.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray

class MobileDataListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                when {
                    event.dataItem.uri.path?.startsWith("/settings/preferences/") == true -> {
                        handleSettingsSync(event)
                    }
                    event.dataItem.uri.path?.startsWith("/rss/sources/") == true -> {
                        handleSourcesSync(event)
                    }
                }
            }
        }
    }

    private fun handleSettingsSync(event: DataEvent) {
        scope.launch {
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            val deviceId = dataMap.getString("deviceId", "")

            if (deviceId == "mobile") {
                val preferenceManager = PreferenceManager(applicationContext)
                val remoteTimestamp = dataMap.getLong("lastModified", 0)
                val localTimestamp = preferenceManager.lastModified.first()

                if (remoteTimestamp < localTimestamp) {
                    return@launch
                }

                preferenceManager.setUiScale(dataMap.getFloat("uiScale", 1.0f))
                preferenceManager.setShowImages(dataMap.getBoolean("showImages", true))
                preferenceManager.setUpdateInterval(dataMap.getInt("updateInterval", 30).toLong())
                preferenceManager.setListViewGrid(dataMap.getBoolean("listViewGrid", false))
                preferenceManager.setMaxCacheSize(dataMap.getInt("maxCacheSize", 20))
                preferenceManager.setFontSize(dataMap.getFloat("fontSize", 14.0f))
                preferenceManager.setBrowserType(dataMap.getString("browserType", "default") ?: "default")
                preferenceManager.setBrowserAvailable(dataMap.getBoolean("browserAvailable", false))
                preferenceManager.setNotificationEnabled(dataMap.getBoolean("notificationEnabled", false))
            }
        }
    }

    private fun handleSourcesSync(event: DataEvent) {
        scope.launch {
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            val deviceId = dataMap.getString("deviceId", "")

            if (deviceId == "mobile") {
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
                val repository = RssRepository(database.sourceDao(), database.articleDao(), com.prof18.rssparser.RssParser())
                repository.syncSources(sources)
            }
        }
    }
}
