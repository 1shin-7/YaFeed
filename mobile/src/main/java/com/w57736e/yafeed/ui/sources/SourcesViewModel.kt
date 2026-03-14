package com.w57736e.yafeed.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.repository.RssSourceRepository
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SourcesViewModel(private val repository: RssSourceRepository) : ViewModel() {
    val sources: StateFlow<List<RssSource>> = repository.getAllSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSource(name: String, url: String, notificationEnabled: Boolean) {
        viewModelScope.launch {
            val maxOrder = sources.value.maxOfOrNull { it.order } ?: -1
            repository.addSource(
                RssSource(
                    name = name,
                    url = url,
                    notificationEnabled = notificationEnabled,
                    order = maxOrder + 1
                )
            )
        }
    }

    fun updateSource(source: RssSource) {
        viewModelScope.launch {
            repository.updateSource(source)
        }
    }

    fun deleteSource(source: RssSource) {
        viewModelScope.launch {
            repository.deleteSource(source)
        }
    }

    fun reorderSources(sources: List<RssSource>) {
        viewModelScope.launch {
            repository.reorderSources(sources)
        }
    }
}
