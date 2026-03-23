package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * 统一的数据同步管理器 - 使用强类型 DataMap
 */
class WearableDataSyncManager(
    context: Context,
    private val connectionManager: WearableConnectionManager
) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    
    companion object {
        private const val TAG = "SYNC_WEAR"
        private const val SETTINGS_PATH = "/yafeed/settings"
        private const val SOURCES_PATH = "/yafeed/sources"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY = 500L
        private const val TIMEOUT_MS = 15_000L
    }
    
    /**
     * 同步设置到 Mobile - 使用强类型 DataMap
     */
    suspend fun syncSettings(bundle: SettingsBundle): Result<Unit> {
        Log.d(TAG, "syncSettings: interval=${bundle.updateInterval}, cache=${bundle.maxCacheSize}")
        
        connectionManager.logSyncEvent(
            SyncEvent(
                timestamp = System.currentTimeMillis(),
                type = SyncType.SETTINGS,
                status = SyncStatus.IN_PROGRESS
            )
        )
        
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "$SETTINGS_PATH/$timestamp"
            
            val request = PutDataMapRequest.create(path).apply {
                dataMap.apply {
                    putBoolean(SyncKeys.SHOW_IMAGES, bundle.showImages)
                    putLong(SyncKeys.UPDATE_INTERVAL, bundle.updateInterval)
                    putBoolean(SyncKeys.LIST_VIEW_GRID, bundle.listViewGrid)
                    putInt(SyncKeys.MAX_CACHE_SIZE, bundle.maxCacheSize)
                    putFloat(SyncKeys.FONT_SIZE, bundle.fontSize)
                    putString(SyncKeys.BROWSER_TYPE, bundle.browserType)
                    putBoolean(SyncKeys.BROWSER_AVAILABLE, bundle.browserAvailable)
                    putBoolean(SyncKeys.NOTIFICATION_ENABLED, bundle.notificationEnabled)
                    putBoolean(SyncKeys.SAVE_IMAGES_ON_FAVORITE, bundle.saveImagesOnFavorite)
                    putBoolean(SyncKeys.USE_ORIGINAL_IMAGE_PREVIEW, bundle.useOriginalImagePreview)
                    putLong(SyncKeys.LAST_MODIFIED, bundle.lastModified)
                    putString(SyncKeys.DEVICE_ID, SyncKeys.DEVICE_WEAR)
                    putLong(SyncKeys.SYNC_TIMESTAMP, timestamp)
                }
            }
            
            withTimeout(TIMEOUT_MS) {
                retryWithBackoff {
                    dataClient.putDataItem(request.asPutDataRequest()).await()
                }
            }
            
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SETTINGS,
                    status = SyncStatus.SUCCESS
                )
            )
            
            Log.d(TAG, "syncSettings: SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "syncSettings: FAILED - ${e.message}")
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SETTINGS,
                    status = SyncStatus.FAILURE,
                    message = e.message ?: "Unknown error"
                )
            )
            Result.failure(e)
        }
    }
    
    /**
     * 同步 RSS 源到 Mobile - 使用强类型 DataMap 数组
     */
    suspend fun syncSources(sources: List<RssSource>): Result<Unit> {
        Log.d(TAG, "syncSources: ${sources.size} sources")
        
        connectionManager.logSyncEvent(
            SyncEvent(
                timestamp = System.currentTimeMillis(),
                type = SyncType.SOURCES,
                status = SyncStatus.IN_PROGRESS
            )
        )
        
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "$SOURCES_PATH/$timestamp"
            
            val sourcesDataMaps = sources.map { source ->
                DataMap().apply {
                    putInt(SyncKeys.SOURCE_ID, source.id)
                    putString(SyncKeys.SOURCE_NAME, source.name)
                    putString(SyncKeys.SOURCE_URL, source.url)
                    putString(SyncKeys.SOURCE_FAVICON, source.faviconUrl ?: "")
                    putBoolean(SyncKeys.SOURCE_NOTIFICATION, source.notificationEnabled)
                    putInt(SyncKeys.SOURCE_ORDER, source.order)
                    putLong(SyncKeys.SOURCE_LAST_MODIFIED, source.lastModified)
                }
            }
            
            val request = PutDataMapRequest.create(path).apply {
                dataMap.apply {
                    putDataMapArrayList(SyncKeys.SOURCES_LIST, ArrayList(sourcesDataMaps))
                    putLong(SyncKeys.LAST_MODIFIED, timestamp)
                    putString(SyncKeys.DEVICE_ID, SyncKeys.DEVICE_WEAR)
                    putLong(SyncKeys.SYNC_TIMESTAMP, timestamp)
                    putInt(SyncKeys.SOURCES_COUNT, sources.size)
                }
            }
            
            withTimeout(TIMEOUT_MS) {
                retryWithBackoff {
                    dataClient.putDataItem(request.asPutDataRequest()).await()
                }
            }
            
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SOURCES,
                    status = SyncStatus.SUCCESS
                )
            )
            
            Log.d(TAG, "syncSources: SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "syncSources: FAILED - ${e.message}")
            connectionManager.logSyncEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SyncType.SOURCES,
                    status = SyncStatus.FAILURE,
                    message = e.message ?: "Unknown error"
                )
            )
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
                if (attempt == maxRetries - 1) {
                    Log.e(TAG, "    Retry exhausted after $maxRetries attempts")
                    throw e
                }
                Log.w(TAG, "    Retry ${attempt + 1}/$maxRetries after ${currentDelay}ms: ${e.message}")
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        throw IllegalStateException("Retry failed")
    }
}

/**
 * 同步数据的键名常量 - 强类型
 */
object SyncKeys {
    // 设备标识
    const val DEVICE_ID = "deviceId"
    const val DEVICE_WEAR = "wear"
    const val DEVICE_MOBILE = "mobile"
    
    // 通用
    const val LAST_MODIFIED = "lastModified"
    const val SYNC_TIMESTAMP = "syncTimestamp"
    
    // 设置
    const val SHOW_IMAGES = "showImages"
    const val UPDATE_INTERVAL = "updateInterval"
    const val LIST_VIEW_GRID = "listViewGrid"
    const val MAX_CACHE_SIZE = "maxCacheSize"
    const val FONT_SIZE = "fontSize"
    const val BROWSER_TYPE = "browserType"
    const val BROWSER_AVAILABLE = "browserAvailable"
    const val NOTIFICATION_ENABLED = "notificationEnabled"
    const val SAVE_IMAGES_ON_FAVORITE = "saveImagesOnFavorite"
    const val USE_ORIGINAL_IMAGE_PREVIEW = "useOriginalImagePreview"
    
    // 源
    const val SOURCES_LIST = "sourcesList"
    const val SOURCES_COUNT = "sourcesCount"
    const val SOURCE_ID = "id"
    const val SOURCE_NAME = "name"
    const val SOURCE_URL = "url"
    const val SOURCE_FAVICON = "faviconUrl"
    const val SOURCE_NOTIFICATION = "notificationEnabled"
    const val SOURCE_ORDER = "order"
    const val SOURCE_LAST_MODIFIED = "lastModified"
}
