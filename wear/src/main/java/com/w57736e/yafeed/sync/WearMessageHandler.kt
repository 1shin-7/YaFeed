package com.w57736e.yafeed.sync

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearMessageHandler(private val context: Context) {
    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    suspend fun sendOpenPageRequest(url: String) {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.forEach { node ->
            messageClient.sendMessage(node.id, "/action/open_page", url.toByteArray()).await()
        }
    }

    suspend fun requestTextInput(prompt: String) {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.firstOrNull()?.let { node ->
            messageClient.sendMessage(node.id, "/action/request_input", prompt.toByteArray()).await()
        }
    }
}
