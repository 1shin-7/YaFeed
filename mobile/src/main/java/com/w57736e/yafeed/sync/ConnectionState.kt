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
    const val UI_SCALE = "uiScale"
    const val SHOW_IMAGES = "showImages"
    const val UPDATE_INTERVAL = "updateInterval"
    const val LIST_VIEW_GRID = "listViewGrid"
    const val MAX_CACHE_SIZE = "maxCacheSize"
    const val FONT_SIZE = "fontSize"
    const val BROWSER_TYPE = "browserType"
    const val BROWSER_AVAILABLE = "browserAvailable"
    const val NOTIFICATION_ENABLED = "notificationEnabled"
    
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
