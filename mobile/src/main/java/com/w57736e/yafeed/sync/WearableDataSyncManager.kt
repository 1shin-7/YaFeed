package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Mobile 端的数据同步管理器 - 使用强类型 DataMap
 */
class WearableDataSyncManager(private val context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    
    companion object {
        private const val TAG = "SYNC_MOBILE"
        private const val SETTINGS_PATH = "/yafeed/settings"
        private const val SOURCES_PATH = "/yafeed/sources"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY = 500L
        private const val TIMEOUT_MS = 15_000L
    }
    
    /**
     * 同步设置到 Wear - 使用强类型 DataMap
     */
    suspend fun syncSettings(bundle: SettingsBundle): Result<Unit> {
        Log.d(TAG, ">>> syncSettings START")
        Log.d(TAG, "    bundle: showImages=${bundle.showImages}, updateInterval=${bundle.updateInterval}")
        Log.d(TAG, "    bundle: fontSize=${bundle.fontSize}, browserType=${bundle.browserType}")
        
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "$SETTINGS_PATH/$timestamp"
            Log.d(TAG, "    path: $path")
            
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
                    putString(SyncKeys.DEVICE_ID, SyncKeys.DEVICE_MOBILE)
                    putLong(SyncKeys.SYNC_TIMESTAMP, timestamp)
                }
            }
            
            val dataRequest = request.asPutDataRequest()
            Log.d(TAG, "    dataRequest.uri: ${dataRequest.uri}")
            Log.d(TAG, "    dataRequest.data.size: ${dataRequest.data?.size ?: 0}")
            
            withTimeout(TIMEOUT_MS) {
                retryWithBackoff {
                    val result = dataClient.putDataItem(dataRequest).await()
                    Log.d(TAG, "    putDataItem result: uri=${result.uri}")
                }
            }
            
            Log.d(TAG, "<<< syncSettings SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "<<< syncSettings FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 同步 RSS 源到 Wear - 使用强类型 DataMap 数组
     */
    suspend fun syncSources(sources: List<RssSource>): Result<Unit> {
        Log.d(TAG, ">>> syncSources START")
        Log.d(TAG, "    sources count: ${sources.size}")
        sources.forEachIndexed { i, s ->
            Log.d(TAG, "    [$i] id=${s.id}, name=${s.name}, url=${s.url}")
        }
        
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "$SOURCES_PATH/$timestamp"
            Log.d(TAG, "    path: $path")
            
            // 将每个源转换为 DataMap
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
                    putString(SyncKeys.DEVICE_ID, SyncKeys.DEVICE_MOBILE)
                    putLong(SyncKeys.SYNC_TIMESTAMP, timestamp)
                    putInt(SyncKeys.SOURCES_COUNT, sources.size)
                }
            }
            
            val dataRequest = request.asPutDataRequest()
            Log.d(TAG, "    dataRequest.uri: ${dataRequest.uri}")
            Log.d(TAG, "    dataRequest.data.size: ${dataRequest.data?.size ?: 0}")
            
            withTimeout(TIMEOUT_MS) {
                retryWithBackoff {
                    val result = dataClient.putDataItem(dataRequest).await()
                    Log.d(TAG, "    putDataItem result: uri=${result.uri}")
                }
            }
            
            Log.d(TAG, "<<< syncSources SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "<<< syncSources FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
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
