package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Mobile 端的消息管理器
 * 使用 DataClient 发送同步请求（更可靠）
 */
class WearableMessageManager(private val context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    
    companion object {
        private const val TAG = "WearableMessage"
        const val REQUEST_SYNC_PATH = "/yafeed/sync_request"
    }
    
    /**
     * 请求同步数据
     * 使用 DataClient 而不是 MessageClient（更可靠）
     */
    suspend fun requestSync(): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "$REQUEST_SYNC_PATH/$timestamp"
            
            val request = PutDataMapRequest.create(path).apply {
                dataMap.apply {
                    putString("deviceId", SyncKeys.DEVICE_MOBILE)
                    putLong("timestamp", timestamp)
                    putBoolean("requesting", true)
                }
            }
            
            dataClient.putDataItem(request.asPutDataRequest()).await()
            
            Log.d(TAG, "Sync request sent via DataClient")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send sync request", e)
            Result.failure(e)
        }
    }
}
