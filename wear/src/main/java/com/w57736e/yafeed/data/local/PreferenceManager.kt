package com.w57736e.yafeed.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val UI_SCALE = floatPreferencesKey("ui_scale")
        val SHOW_IMAGES = booleanPreferencesKey("show_images")
        val UPDATE_INTERVAL = longPreferencesKey("update_interval")
        val LIST_VIEW_GRID = booleanPreferencesKey("list_view_grid")
        val MAX_CACHE_SIZE = intPreferencesKey("max_cache_size")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val BROWSER_TYPE = stringPreferencesKey("browser_type")
        val BROWSER_AVAILABLE = booleanPreferencesKey("browser_available")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val USE_ORIGINAL_IMAGE_PREVIEW = booleanPreferencesKey("use_original_image_preview")
        val SAVE_IMAGES_ON_FAVORITE = booleanPreferencesKey("save_images_on_favorite")
        val LAST_MODIFIED = longPreferencesKey("last_modified")
    }

    val uiScale: Flow<Float> = dataStore.data.map { it[UI_SCALE] ?: 1.0f }
    val showImages: Flow<Boolean> = dataStore.data.map { it[SHOW_IMAGES] ?: true }
    val updateInterval: Flow<Long> = dataStore.data.map { it[UPDATE_INTERVAL] ?: 30L }
    val isGridView: Flow<Boolean> = dataStore.data.map { it[LIST_VIEW_GRID] ?: false }
    val maxCacheSize: Flow<Int> = dataStore.data.map { it[MAX_CACHE_SIZE] ?: 20 }
    val fontSize: Flow<Float> = dataStore.data.map { it[FONT_SIZE] ?: 14f }
    val browserType: Flow<String> = dataStore.data.map { it[BROWSER_TYPE] ?: "default" }
    val browserAvailable: Flow<Boolean> = dataStore.data.map { it[BROWSER_AVAILABLE] ?: false }
    val notificationEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFICATION_ENABLED] ?: false }
    val useOriginalImagePreview: Flow<Boolean> = dataStore.data.map { it[USE_ORIGINAL_IMAGE_PREVIEW] ?: false }
    val saveImagesOnFavorite: Flow<Boolean> = dataStore.data.map { it[SAVE_IMAGES_ON_FAVORITE] ?: false }
    val lastModified: Flow<Long> = dataStore.data.map { it[LAST_MODIFIED] ?: 0L }

    suspend fun setUiScale(scale: Float) {
        dataStore.edit { it[UI_SCALE] = scale; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setShowImages(show: Boolean) {
        dataStore.edit { it[SHOW_IMAGES] = show; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setUpdateInterval(interval: Long) {
        dataStore.edit { it[UPDATE_INTERVAL] = interval; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setMaxCacheSize(size: Int) {
        dataStore.edit { it[MAX_CACHE_SIZE] = size; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setFontSize(size: Float) {
        dataStore.edit { it[FONT_SIZE] = size; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setBrowserType(type: String) {
        dataStore.edit { it[BROWSER_TYPE] = type; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setBrowserAvailable(available: Boolean) {
        dataStore.edit { it[BROWSER_AVAILABLE] = available; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATION_ENABLED] = enabled; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setUseOriginalImagePreview(enabled: Boolean) {
        dataStore.edit { it[USE_ORIGINAL_IMAGE_PREVIEW] = enabled; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setSaveImagesOnFavorite(enabled: Boolean) {
        dataStore.edit { it[SAVE_IMAGES_ON_FAVORITE] = enabled; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun setListViewGrid(grid: Boolean) {
        dataStore.edit { it[LIST_VIEW_GRID] = grid; it[LAST_MODIFIED] = System.currentTimeMillis() }
    }

    suspend fun toggleViewMode() {
        dataStore.edit {
            val current = it[LIST_VIEW_GRID] ?: false
            it[LIST_VIEW_GRID] = !current
            it[LAST_MODIFIED] = System.currentTimeMillis()
        }
    }
}
