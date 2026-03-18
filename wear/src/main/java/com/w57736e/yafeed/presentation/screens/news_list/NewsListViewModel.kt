package com.w57736e.yafeed.presentation.screens.news_list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.data.repository.isNetworkAvailable
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NewsListUiState(
    val articles: List<RssArticle> = emptyList(),
    val source: RssSource? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class NewsListViewModel(
    private val repository: RssRepository,
    private val preferenceManager: PreferenceManager,
    private val context: Context
) : ViewModel() {

    private val _sourceId = MutableStateFlow<Int?>(null)
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<NewsListUiState> = _sourceId.flatMapLatest { id ->
        if (id == null) flowOf(NewsListUiState())
        else {
            combine(
                repository.getCachedArticles(id),
                flow { emit(repository.getSourceById(id)) }
            ) { articles, source ->
                NewsListUiState(articles = articles, source = source, isLoading = false)
            }
        }
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isLoading = refreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NewsListUiState())

    fun setSourceId(id: Int) {
        val isNewSource = _sourceId.value != id
        _sourceId.value = id
        if (isNewSource) {
            refreshArticles()
        }
    }

    fun refreshArticles() {
        val id = _sourceId.value ?: return
        if (!isNetworkAvailable(context)) {
            return
        }
        viewModelScope.launch {
            _isRefreshing.value = true
            val cacheSize = preferenceManager.maxCacheSize.first()
            repository.fetchAndCache(id, cacheSize)
            _isRefreshing.value = false
        }
    }
}
