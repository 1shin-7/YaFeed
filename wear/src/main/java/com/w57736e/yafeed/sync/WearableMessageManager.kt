package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * 统一的消息管理器
 * 负责通过 MessageClient 发送消息
 * 替代原来的 WearMessageHandler
 */
class WearableMessageManager(private val context: Context) {
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    
    companion object {
        private const val TAG = "WearableMessage"
        const val OPEN_PAGE_PATH = "/yafeed/open_page"
        const val REQUEST_INPUT_PATH = "/yafeed/request_input"
        const val ACK_PATH = "/yafeed/ack"
        const val REQUEST_SYNC_PATH = "/yafeed/request_sync"
    }
    
    /**
     * 在手机上打开页面
     */
    suspend fun sendOpenPage(url: String): Result<Unit> {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                return Result.failure(Exception("No connected devices"))
            }
            
            nodes.forEach { node ->
                messageClient.sendMessage(
                    node.id,
                    OPEN_PAGE_PATH,
                    url.toByteArray()
                ).await()
            }
            
            Log.d(TAG, "Open page request sent: $url")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send open page request", e)
            Result.failure(e)
        }
    }
    
    /**
     * 请求文本输入
     */
    suspend fun requestInput(prompt: String): Result<Unit> {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            val node = nodes.firstOrNull() 
                ?: return Result.failure(Exception("No connected devices"))
            
            messageClient.sendMessage(
                node.id,
                REQUEST_INPUT_PATH,
                prompt.toByteArray()
            ).await()
            
            Log.d(TAG, "Input request sent: $prompt")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send input request", e)
            Result.failure(e)
        }
    }
    
    /**
     * 发送确认消息
     */
    suspend fun sendAck(nodeId: String, message: String): Result<Unit> {
        return try {
            messageClient.sendMessage(
                nodeId,
                ACK_PATH,
                message.toByteArray()
            ).await()
            
            Log.d(TAG, "Ack sent to $nodeId: $message")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send ack", e)
            Result.failure(e)
        }
    }
    
    /**
     * 请求同步数据
     */
    suspend fun requestSync(): Result<Unit> {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            val node = nodes.firstOrNull() 
                ?: return Result.failure(Exception("No connected devices"))
            
            messageClient.sendMessage(
                node.id,
                REQUEST_SYNC_PATH,
                "sync".toByteArray()
            ).await()
            
            Log.d(TAG, "Sync request sent")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send sync request", e)
            Result.failure(e)
        }
    }
}
