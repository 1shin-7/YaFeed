package com.w57736e.yafeed.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.w57736e.yafeed.R

class MobileMessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/action/open_page" -> {
                val url = String(messageEvent.data)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            "/action/request_input" -> {
                val prompt = String(messageEvent.data)
                showInputNotification(prompt, messageEvent.sourceNodeId)
            }
        }
    }

    private fun showInputNotification(prompt: String, nodeId: String) {
        val channelId = "wear_input"
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Wear Input Requests",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val remoteInput = RemoteInput.Builder("input_text")
            .setLabel(prompt)
            .build()

        val intent = Intent(this, MobileMessageListenerService::class.java).apply {
            action = "REPLY_ACTION"
            putExtra("node_id", nodeId)
        }

        val pendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Input from Watch")
            .setContentText(prompt)
            .addAction(
                NotificationCompat.Action.Builder(
                    0, "Reply", pendingIntent
                ).addRemoteInput(remoteInput).build()
            )
            .build()

        notificationManager.notify(1, notification)
    }
}
