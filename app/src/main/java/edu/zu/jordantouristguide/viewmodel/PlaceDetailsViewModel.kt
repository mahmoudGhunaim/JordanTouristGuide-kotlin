package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.data.model.Review
import edu.zu.jordantouristguide.repository.AuthRepository
import edu.zu.jordantouristguide.repository.FavoritesRepository
import edu.zu.jordantouristguide.repository.PlaceRepository
import edu.zu.jordantouristguide.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceDetailsUiState(
    val place: UiState<Place> = UiState.Loading,
    val isNotFound: Boolean = false,
    val isFavorite: Boolean = false,
    /** null while reviews are still loading. */
    val reviews: List<Review>? = null,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class PlaceDetailsViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val favoritesRepository: FavoritesRepository,
    reviewRepository: ReviewRepository,
    authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Comes from DetailsRoute(placeId). */
    val placeId: String = checkNotNull(savedStateHandle["placeId"]) { "placeId is required" }

    private val placeState = MutableStateFlow<UiState<Place>>(UiState.Loading)
    private val notFound = MutableStateFlow(false)

    private val reviews = reviewRepository.observeReviews(placeId)
        .map<List<Review>, List<Review>?> { it }
        .onStart { emit(null) }
        .catch { emit(emptyList()) }

    private val placeAndFlag = combine(placeState, notFound) { place, missing -> place to missing }

    val uiState: StateFlow<PlaceDetailsUiState> = combine(
        placeAndFlag,
        favoritesRepository.observeIsFavorite(placeId),
        reviews,
        authRepository.currentUser,
    ) { (place, missing), isFavorite, reviewList, user ->
        PlaceDetailsUiState(
            place = place,
            isNotFound = missing,
            isFavorite = isFavorite,
            reviews = reviewList,
            isLoggedIn = user != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaceDetailsUiState())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            placeState.value = UiState.Loading
            notFound.value = false
            try {
                val place = placeRepository.getPlace(placeId)
                if (place == null) {
                    notFound.value = true
                    placeState.value = UiState.Error()
                } else {
                    placeState.value = UiState.Success(place)
                }
            } catch (e: Exception) {
                placeState.value = UiState.Error(e.message)
            }
        }
    }

    /** Adds or removes the place from Room favorites. */
    fun toggleFavorite() {
        val place = (placeState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            if (uiState.value.isFavorite) {
                favoritesRepository.removeFavorite(place.id)
            } else {
                favoritesRepository.addFavorite(place)
            }
        }
    }
}
