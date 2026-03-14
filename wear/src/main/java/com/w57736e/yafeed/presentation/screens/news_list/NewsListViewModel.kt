package com.w57736e.yafeed.presentation.screens.news_list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.data.repository.isNetworkAvailable
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsListUiState(
    val articles: List<RssArticle> = emptyList(),
    val source: RssSource? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NewsListViewModel(
    private val repository: RssRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsListUiState())
    val uiState: StateFlow<NewsListUiState> = _uiState

    fun fetchArticles(url: String) {
        if (!isNetworkAvailable(context)) {
            _uiState.update { it.copy(isLoading = false, error = "No network connection. Check your Wi-Fi or Bluetooth.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val articles = repository.fetchArticles(url)
                _uiState.update { it.copy(articles = articles, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load: ${e.localizedMessage}") }
            }
        }
    }
}
