package com.w57736e.yafeed.data.repository

import com.w57736e.yafeed.data.local.PreferenceManager
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val preferenceManager: PreferenceManager) {
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

    suspend fun setUiScale(value: Float) = preferenceManager.setUiScale(value)
    suspend fun setShowImages(value: Boolean) = preferenceManager.setShowImages(value)
    suspend fun setUpdateInterval(value: Int) = preferenceManager.setUpdateInterval(value)
    suspend fun setListViewGrid(value: Boolean) = preferenceManager.setListViewGrid(value)
    suspend fun setMaxCacheSize(value: Int) = preferenceManager.setMaxCacheSize(value)
    suspend fun setFontSize(value: Float) = preferenceManager.setFontSize(value)
    suspend fun setBrowserType(value: String) = preferenceManager.setBrowserType(value)
    suspend fun setBrowserAvailable(value: Boolean) = preferenceManager.setBrowserAvailable(value)
    suspend fun setNotificationEnabled(value: Boolean) = preferenceManager.setNotificationEnabled(value)
}
