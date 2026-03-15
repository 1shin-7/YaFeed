package com.w57736e.yafeed.data.repository

import android.util.Log
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.sync.WearSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val preferenceManager: PreferenceManager,
    private val wearSyncManager: WearSyncManager
) {
    val uiScale: Flow<Float> = preferenceManager.uiScale
    val showImages: Flow<Boolean> = preferenceManager.showImages
    val updateInterval: Flow<Int> = preferenceManager.updateInterval
    val listViewGrid: Flow<Boolean> = preferenceManager.listViewGrid
    val maxCacheSize: Flow<Int> = preferenceManager.maxCacheSize
    val fontSize: Flow<Float> = preferenceManager.fontSize
    val browserType: Flow<String> = preferenceManager.browserType
    val browserAvailable: Flow<Boolean> = preferenceManager.browserAvailable
    val notificationEnabled: Flow<Boolean> = preferenceManager.notificationEnabled
    val lastModified: Flow<Long> = preferenceManager.lastModified

    suspend fun setUiScale(value: Float) {
        preferenceManager.setUiScale(value)
        syncToWear()
    }

    suspend fun setShowImages(value: Boolean) {
        preferenceManager.setShowImages(value)
        syncToWear()
    }

    suspend fun setUpdateInterval(value: Int) {
        preferenceManager.setUpdateInterval(value)
        syncToWear()
    }

    suspend fun setListViewGrid(value: Boolean) {
        preferenceManager.setListViewGrid(value)
        syncToWear()
    }

    suspend fun setMaxCacheSize(value: Int) {
        preferenceManager.setMaxCacheSize(value)
        syncToWear()
    }

    suspend fun setFontSize(value: Float) {
        preferenceManager.setFontSize(value)
        syncToWear()
    }

    suspend fun setBrowserType(value: String) {
        preferenceManager.setBrowserType(value)
        syncToWear()
    }

    suspend fun setBrowserAvailable(value: Boolean) {
        preferenceManager.setBrowserAvailable(value)
        syncToWear()
    }

    suspend fun setNotificationEnabled(value: Boolean) {
        preferenceManager.setNotificationEnabled(value)
        syncToWear()
    }

    private suspend fun syncToWear() {
        try {
            Log.d("SettingsSync", "Syncing settings to Wear...")
            wearSyncManager.syncSettings(
                uiScale = uiScale.first(),
                showImages = showImages.first(),
                updateInterval = updateInterval.first(),
                listViewGrid = listViewGrid.first(),
                maxCacheSize = maxCacheSize.first(),
                fontSize = fontSize.first(),
                browserType = browserType.first(),
                browserAvailable = browserAvailable.first(),
                notificationEnabled = notificationEnabled.first(),
                lastModified = lastModified.first()
            )
            Log.d("SettingsSync", "Settings synced successfully")
        } catch (e: Exception) {
            Log.e("SettingsSync", "Failed to sync settings", e)
        }
    }
}
