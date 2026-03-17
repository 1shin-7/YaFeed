package com.w57736e.yafeed.sync

import java.security.MessageDigest
import java.util.UUID

enum class ConnectionStatus {
    DISCONNECTED,
    SEARCHING,
    CONNECTED
}

data class SettingsBundle(
    val uiScale: Float,
    val showImages: Boolean,
    val updateInterval: Int,
    val listViewGrid: Boolean,
    val maxCacheSize: Int,
    val fontSize: Float,
    val browserType: String,
    val browserAvailable: Boolean,
    val notificationEnabled: Boolean,
    val lastModified: Long,
    val checksum: String
) {
    companion object {
        fun create(
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
        ): SettingsBundle {
            val data = "$uiScale|$showImages|$updateInterval|$listViewGrid|$maxCacheSize|$fontSize|$browserType|$browserAvailable|$notificationEnabled|$lastModified"
            val checksum = MessageDigest.getInstance("MD5")
                .digest(data.toByteArray())
                .joinToString("") { "%02x".format(it) }
            return SettingsBundle(
                uiScale, showImages, updateInterval, listViewGrid, maxCacheSize,
                fontSize, browserType, browserAvailable, notificationEnabled, lastModified, checksum
            )
        }
    }
}

data class ConnectedNode(
    val id: String,
    val displayName: String,
    val isNearby: Boolean
)

data class ConnectionState(
    val status: ConnectionStatus,
    val nodes: List<ConnectedNode>,
    val lastCheckTime: Long
)

data class SyncEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long,
    val type: SyncType,
    val status: SyncStatus,
    val message: String = ""
)

enum class SyncType {
    SETTINGS,
    SOURCES,
    MESSAGE
}

enum class SyncStatus {
    SUCCESS,
    FAILURE,
    IN_PROGRESS
}
