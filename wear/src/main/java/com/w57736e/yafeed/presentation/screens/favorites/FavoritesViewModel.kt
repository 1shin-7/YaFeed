package com.w57736e.yafeed.presentation.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.domain.model.FavoriteArticle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<FavoriteArticle> = emptyList(),
    val undoState: UndoState? = null
)

data class UndoState(
    val article: FavoriteArticle,
    val expiresAt: Long
)

class FavoritesViewModel(
    private val repository: RssRepository
) : ViewModel() {

    private val _undoState = MutableStateFlow<UndoState?>(null)
    private val _pendingDelete = MutableStateFlow<Set<String>>(emptySet())
    private var deleteJob: Job? = null

    val uiState: StateFlow<FavoritesUiState> = combine(
        repository.getAllFavorites(),
        _undoState,
        _pendingDelete
    ) { favorites, undo, pending ->
        FavoritesUiState(
            favorites = favorites.filterNot { it.link in pending },
            undoState = undo
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState())

    fun deleteFavorite(favorite: FavoriteArticle) {
        deleteJob?.cancel()
        _pendingDelete.value = _pendingDelete.value + favorite.link
        _undoState.value = UndoState(favorite, System.currentTimeMillis() + 3000)

        deleteJob = viewModelScope.launch {
            delay(3000)
            if (_undoState.value?.article == favorite) {
                repository.deleteFavorite(favorite)
                _pendingDelete.value = _pendingDelete.value - favorite.link
                _undoState.value = null
            }
        }
    }

    fun undoDelete() {
        deleteJob?.cancel()
        _undoState.value?.let { undo ->
            _pendingDelete.value = _pendingDelete.value - undo.article.link
        }
        _undoState.value = null
    }
}
