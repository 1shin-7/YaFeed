package com.w57736e.yafeed.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val showImages: StateFlow<Boolean> = repository.showImages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val updateInterval: StateFlow<Long> = repository.updateInterval.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30L)
    val listViewGrid: StateFlow<Boolean> = repository.listViewGrid.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val maxCacheSize: StateFlow<Int> = repository.maxCacheSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)
    val fontSize: StateFlow<Float> = repository.fontSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14f)
    val browserType: StateFlow<String> = repository.browserType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")
    val notificationEnabled: StateFlow<Boolean> = repository.notificationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val saveImagesOnFavorite: StateFlow<Boolean> = repository.saveImagesOnFavorite.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val useOriginalImagePreview: StateFlow<Boolean> = repository.useOriginalImagePreview.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setShowImages(value: Boolean) = viewModelScope.launch { repository.setShowImages(value) }
    fun setUpdateInterval(value: Long) = viewModelScope.launch { repository.setUpdateInterval(value) }
    fun setListViewGrid(value: Boolean) = viewModelScope.launch { repository.setListViewGrid(value) }
    fun setMaxCacheSize(value: Int) = viewModelScope.launch { repository.setMaxCacheSize(value) }
    fun setFontSize(value: Float) = viewModelScope.launch { repository.setFontSize(value) }
    fun setBrowserType(value: String) = viewModelScope.launch { repository.setBrowserType(value) }
    fun setNotificationEnabled(value: Boolean) = viewModelScope.launch { repository.setNotificationEnabled(value) }
    fun setSaveImagesOnFavorite(value: Boolean) = viewModelScope.launch { repository.setSaveImagesOnFavorite(value) }
    fun setUseOriginalImagePreview(value: Boolean) = viewModelScope.launch { repository.setUseOriginalImagePreview(value) }
}
