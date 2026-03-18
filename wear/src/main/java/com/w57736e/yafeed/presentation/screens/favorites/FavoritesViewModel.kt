package com.w57736e.yafeed.presentation.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.domain.model.FavoriteArticle
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

    val uiState: StateFlow<FavoritesUiState> = combine(
        repository.getAllFavorites(),
        _undoState
    ) { favorites, undo ->
        FavoritesUiState(favorites = favorites, undoState = undo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState())

    fun deleteFavorite(favorite: FavoriteArticle) {
        _undoState.value = UndoState(favorite, System.currentTimeMillis() + 3000)
        viewModelScope.launch {
            delay(3000)
            if (_undoState.value?.article == favorite) {
                repository.deleteFavorite(favorite)
                _undoState.value = null
            }
        }
    }

    fun undoDelete() {
        _undoState.value = null
    }
}
