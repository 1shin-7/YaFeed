package com.w57736e.yafeed.presentation.screens.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.sync.WearableDataSyncManager
import com.w57736e.yafeed.sync.SettingsBundle
import com.w57736e.yafeed.sync.WearableConnectionManager
import com.w57736e.yafeed.sync.WearableMessageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WearConnectionDebugViewModel(
    private val connectionManager: WearableConnectionManager,
    private val syncManager: WearableDataSyncManager,
    private val messageManager: WearableMessageManager,
    private val prefManager: PreferenceManager,
    private val repository: RssRepository
) : ViewModel() {

    val connectionState = connectionManager.connectionState
    val syncHistory = connectionManager.syncHistory

    init {
        viewModelScope.launch {
            connectionManager.startMonitoring()
        }
    }

    fun syncSettings() = viewModelScope.launch {
        val bundle = SettingsBundle.create(
            uiScale = prefManager.uiScale.first(),
            showImages = prefManager.showImages.first(),
            updateInterval = prefManager.updateInterval.first().toInt(),
            listViewGrid = prefManager.isGridView.first(),
            maxCacheSize = prefManager.maxCacheSize.first(),
            fontSize = prefManager.fontSize.first(),
            browserType = prefManager.browserType.first(),
            browserAvailable = prefManager.browserAvailable.first(),
            notificationEnabled = prefManager.notificationEnabled.first(),
            lastModified = System.currentTimeMillis()
        )
        syncManager.syncSettings(bundle)
    }

    fun syncSources() = viewModelScope.launch {
        val sources = repository.getAllSources().first()
        syncManager.syncSources(sources)
    }

    fun openPageOnPhone() = viewModelScope.launch {
        messageManager.sendOpenPage("https://github.com/w57736e/YaFeed")
    }

    fun requestTextInput() = viewModelScope.launch {
        messageManager.requestInput("Enter text from phone:")
    }
}
