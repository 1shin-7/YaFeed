package com.w57736e.yafeed.data.repository

import android.util.Log
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.sync.WearableDataSyncManager
import com.w57736e.yafeed.sync.SettingsBundle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val preferenceManager: PreferenceManager,
    private val wearableDataSyncManager: WearableDataSyncManager
) {
    val showImages: Flow<Boolean> = preferenceManager.showImages
    val updateInterval: Flow<Long> = preferenceManager.updateInterval
    val listViewGrid: Flow<Boolean> = preferenceManager.listViewGrid
    val maxCacheSize: Flow<Int> = preferenceManager.maxCacheSize
    val fontSize: Flow<Float> = preferenceManager.fontSize
    val browserType: Flow<String> = preferenceManager.browserType
    val browserAvailable: Flow<Boolean> = preferenceManager.browserAvailable
    val notificationEnabled: Flow<Boolean> = preferenceManager.notificationEnabled
    val saveImagesOnFavorite: Flow<Boolean> = preferenceManager.saveImagesOnFavorite
    val useOriginalImagePreview: Flow<Boolean> = preferenceManager.useOriginalImagePreview
    val lastModified: Flow<Long> = preferenceManager.lastModified

    suspend fun setShowImages(value: Boolean) {
        preferenceManager.setShowImages(value)
        syncToWear()
    }

    suspend fun setUpdateInterval(value: Long) {
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

    suspend fun setSaveImagesOnFavorite(value: Boolean) {
        preferenceManager.setSaveImagesOnFavorite(value)
        syncToWear()
    }

    suspend fun setUseOriginalImagePreview(value: Boolean) {
        preferenceManager.setUseOriginalImagePreview(value)
        syncToWear()
    }

    private suspend fun syncToWear() {
        try {
            Log.d("SettingsSync", "Syncing settings to Wear...")
            val bundle = SettingsBundle.create(
                showImages = showImages.first(),
                updateInterval = updateInterval.first(),
                listViewGrid = listViewGrid.first(),
                maxCacheSize = maxCacheSize.first(),
                fontSize = fontSize.first(),
                browserType = browserType.first(),
                browserAvailable = browserAvailable.first(),
                notificationEnabled = notificationEnabled.first(),
                saveImagesOnFavorite = saveImagesOnFavorite.first(),
                useOriginalImagePreview = useOriginalImagePreview.first(),
                lastModified = lastModified.first()
            )
            wearableDataSyncManager.syncSettings(bundle)
            Log.d("SettingsSync", "Settings synced successfully")
        } catch (e: Exception) {
            Log.e("SettingsSync", "Failed to sync settings", e)
        }
    }
}
