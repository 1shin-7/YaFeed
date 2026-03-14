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

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllSources(),
        preferenceManager.isGridView,
        preferenceManager.uiScale
    ) { sources, isGrid, scale ->
        HomeUiState(sources, isGrid, false, scale)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun toggleViewMode() {
        viewModelScope.launch {
            preferenceManager.toggleViewMode()
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            uiState.value.sources.forEach { source ->
                repository.updateSourceInfo(source)
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
