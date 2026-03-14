package com.w57736e.yafeed.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val uiScale: StateFlow<Float> = repository.uiScale.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)
    val showImages: StateFlow<Boolean> = repository.showImages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val updateInterval: StateFlow<Int> = repository.updateInterval.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)
    val listViewGrid: StateFlow<Boolean> = repository.listViewGrid.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val maxCacheSize: StateFlow<Int> = repository.maxCacheSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)
    val fontSize: StateFlow<Float> = repository.fontSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14.0f)
    val browserType: StateFlow<String> = repository.browserType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")
    val notificationEnabled: StateFlow<Boolean> = repository.notificationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setUiScale(value: Float) = viewModelScope.launch { repository.setUiScale(value) }
    fun setShowImages(value: Boolean) = viewModelScope.launch { repository.setShowImages(value) }
    fun setUpdateInterval(value: Int) = viewModelScope.launch { repository.setUpdateInterval(value) }
    fun setListViewGrid(value: Boolean) = viewModelScope.launch { repository.setListViewGrid(value) }
    fun setMaxCacheSize(value: Int) = viewModelScope.launch { repository.setMaxCacheSize(value) }
    fun setFontSize(value: Float) = viewModelScope.launch { repository.setFontSize(value) }
    fun setBrowserType(value: String) = viewModelScope.launch { repository.setBrowserType(value) }
    fun setNotificationEnabled(value: Boolean) = viewModelScope.launch { repository.setNotificationEnabled(value) }
}
