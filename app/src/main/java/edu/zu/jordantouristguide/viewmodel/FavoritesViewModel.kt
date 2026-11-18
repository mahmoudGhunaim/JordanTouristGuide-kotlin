package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.model.FavoritePlace
import edu.zu.jordantouristguide.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Favorites come from Room, so this screen works with no internet at all. */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    val uiState: StateFlow<UiState<List<FavoritePlace>>> = favoritesRepository.observeFavorites()
        .map<List<FavoritePlace>, UiState<List<FavoritePlace>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun removeFavorite(placeId: String) {
        viewModelScope.launch { favoritesRepository.removeFavorite(placeId) }
    }
}
