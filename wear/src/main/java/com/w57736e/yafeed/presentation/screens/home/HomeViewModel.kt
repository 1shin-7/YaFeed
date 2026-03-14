package com.w57736e.yafeed.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val sources: List<RssSource> = emptyList(),
    val isGridView: Boolean = false,
    val isLoading: Boolean = false,
    val uiScale: Float = 1.0f
)

class HomeViewModel(
    private val repository: RssRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllSources(),
        preferenceManager.isGridView,
        preferenceManager.uiScale,
        _isRefreshing
    ) { sources, isGrid, scale, refreshing ->
        HomeUiState(sources, isGrid, refreshing, scale)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        refreshAll()
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            preferenceManager.toggleViewMode()
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val cacheSize = preferenceManager.maxCacheSize.first()
            val sources = uiState.value.sources.ifEmpty {
                repository.getAllSources().first()
            }
            sources.forEach { source ->
                repository.fetchAndCache(source.id, cacheSize)
            }
            _isRefreshing.value = false
        }
    }
}
