package com.w57736e.yafeed.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {
    private val showImagesKey = booleanPreferencesKey("show_images")
    private val updateIntervalKey = longPreferencesKey("update_interval")
    private val listViewGridKey = booleanPreferencesKey("list_view_grid")
    private val maxCacheSizeKey = intPreferencesKey("max_cache_size")
    private val fontSizeKey = floatPreferencesKey("font_size")
    private val browserTypeKey = stringPreferencesKey("browser_type")
    private val browserAvailableKey = booleanPreferencesKey("browser_available")
    private val notificationEnabledKey = booleanPreferencesKey("notification_enabled")
    private val saveImagesOnFavoriteKey = booleanPreferencesKey("save_images_on_favorite")
    private val useOriginalImagePreviewKey = booleanPreferencesKey("use_original_image_preview")
    private val lastModifiedKey = longPreferencesKey("last_modified")

    val showImages: Flow<Boolean> = context.dataStore.data.map { it[showImagesKey] ?: true }
    val updateInterval: Flow<Long> = context.dataStore.data.map { it[updateIntervalKey] ?: 30L }
    val listViewGrid: Flow<Boolean> = context.dataStore.data.map { it[listViewGridKey] ?: false }
    val maxCacheSize: Flow<Int> = context.dataStore.data.map { it[maxCacheSizeKey] ?: 20 }
    val fontSize: Flow<Float> = context.dataStore.data.map { it[fontSizeKey] ?: 14f }
    val browserType: Flow<String> = context.dataStore.data.map { it[browserTypeKey] ?: "default" }
    val browserAvailable: Flow<Boolean> = context.dataStore.data.map { it[browserAvailableKey] ?: false }
    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[notificationEnabledKey] ?: false }
    val saveImagesOnFavorite: Flow<Boolean> = context.dataStore.data.map { it[saveImagesOnFavoriteKey] ?: false }
    val useOriginalImagePreview: Flow<Boolean> = context.dataStore.data.map { it[useOriginalImagePreviewKey] ?: false }
    val lastModified: Flow<Long> = context.dataStore.data.map { it[lastModifiedKey] ?: 0L }

    suspend fun setShowImages(value: Boolean) {
        context.dataStore.edit { it[showImagesKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setUpdateInterval(value: Long) {
        context.dataStore.edit { it[updateIntervalKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setListViewGrid(value: Boolean) {
        context.dataStore.edit { it[listViewGridKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setMaxCacheSize(value: Int) {
        context.dataStore.edit { it[maxCacheSizeKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setFontSize(value: Float) {
        context.dataStore.edit { it[fontSizeKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setBrowserType(value: String) {
        context.dataStore.edit { it[browserTypeKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setBrowserAvailable(value: Boolean) {
        context.dataStore.edit { it[browserAvailableKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setNotificationEnabled(value: Boolean) {
        context.dataStore.edit { it[notificationEnabledKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setSaveImagesOnFavorite(value: Boolean) {
        context.dataStore.edit { it[saveImagesOnFavoriteKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }

    suspend fun setUseOriginalImagePreview(value: Boolean) {
        context.dataStore.edit { it[useOriginalImagePreviewKey] = value; it[lastModifiedKey] = System.currentTimeMillis() }
    }
}
