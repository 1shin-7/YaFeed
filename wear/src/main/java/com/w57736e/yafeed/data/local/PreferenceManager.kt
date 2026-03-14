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
    }

    val uiScale: Flow<Float> = dataStore.data.map { it[UI_SCALE] ?: 1.0f }
    val showImages: Flow<Boolean> = dataStore.data.map { it[SHOW_IMAGES] ?: true }
    val updateInterval: Flow<Long> = dataStore.data.map { it[UPDATE_INTERVAL] ?: 30L }
    val isGridView: Flow<Boolean> = dataStore.data.map { it[LIST_VIEW_GRID] ?: false }
    val maxCacheSize: Flow<Int> = dataStore.data.map { it[MAX_CACHE_SIZE] ?: 20 }
    val fontSize: Flow<Float> = dataStore.data.map { it[FONT_SIZE] ?: 14f }

    suspend fun setUiScale(scale: Float) {
        dataStore.edit { it[UI_SCALE] = scale }
    }

    suspend fun setShowImages(show: Boolean) {
        dataStore.edit { it[SHOW_IMAGES] = show }
    }

    suspend fun setUpdateInterval(interval: Long) {
        dataStore.edit { it[UPDATE_INTERVAL] = interval }
    }

    suspend fun setMaxCacheSize(size: Int) {
        dataStore.edit { it[MAX_CACHE_SIZE] = size }
    }

    suspend fun setFontSize(size: Float) {
        dataStore.edit { it[FONT_SIZE] = size }
    }

    suspend fun toggleViewMode() {
        dataStore.edit { 
            val current = it[LIST_VIEW_GRID] ?: false
            it[LIST_VIEW_GRID] = !current
        }
    }
}
