package com.w57736e.yafeed.sync

import java.security.MessageDigest
import java.util.UUID

enum class ConnectionStatus {
    DISCONNECTED,
    SEARCHING,
    CONNECTED
}

data class SettingsBundle(
    val showImages: Boolean,
    val updateInterval: Long,
    val listViewGrid: Boolean,
    val maxCacheSize: Int,
    val fontSize: Float,
    val browserType: String,
    val browserAvailable: Boolean,
    val notificationEnabled: Boolean,
    val saveImagesOnFavorite: Boolean,
    val useOriginalImagePreview: Boolean,
    val lastModified: Long,
    val checksum: String
) {
    companion object {
        fun create(
            showImages: Boolean,
            updateInterval: Long,
            listViewGrid: Boolean,
            maxCacheSize: Int,
            fontSize: Float,
            browserType: String,
            browserAvailable: Boolean,
            notificationEnabled: Boolean,
            saveImagesOnFavorite: Boolean,
            useOriginalImagePreview: Boolean,
            lastModified: Long
        ): SettingsBundle {
            val data = "$showImages|$updateInterval|$listViewGrid|$maxCacheSize|$fontSize|$browserType|$browserAvailable|$notificationEnabled|$saveImagesOnFavorite|$useOriginalImagePreview|$lastModified"
            val checksum = MessageDigest.getInstance("MD5")
                .digest(data.toByteArray())
                .joinToString("") { "%02x".format(it) }
            return SettingsBundle(
                showImages, updateInterval, listViewGrid, maxCacheSize,
                fontSize, browserType, browserAvailable, notificationEnabled,
                saveImagesOnFavorite, useOriginalImagePreview, lastModified, checksum
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
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val nodes: List<ConnectedNode> = emptyList(),
    val lastCheckTime: Long = System.currentTimeMillis()
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
