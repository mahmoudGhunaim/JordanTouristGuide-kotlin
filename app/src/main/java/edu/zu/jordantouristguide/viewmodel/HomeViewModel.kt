package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.data.model.PlaceFilter
import edu.zu.jordantouristguide.repository.AuthRepository
import edu.zu.jordantouristguide.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeContent(
    val topRated: List<Place>,
    /** All places, filtered by the selected category chip. */
    val places: List<Place>,
)

data class HomeUiState(
    val content: UiState<HomeContent> = UiState.Loading,
    val selectedCategory: Category? = null,
    val userName: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val placesState = MutableStateFlow<UiState<List<Place>>>(UiState.Loading)
    private val selectedCategory = MutableStateFlow<Category?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        placesState,
        selectedCategory,
        authRepository.currentUser,
    ) { places, category, user ->
        HomeUiState(
            content = when (places) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> places
                is UiState.Success -> UiState.Success(
                    HomeContent(
                        topRated = PlaceFilter.topRated(places.data),
                        places = PlaceFilter.filter(places.data, category = category),
                    ),
                )
            },
            selectedCategory = category,
            userName = user?.name,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        loadPlaces()
    }

    fun loadPlaces(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            placesState.value = UiState.Loading
            placesState.value = try {
                UiState.Success(placeRepository.getPlaces(forceRefresh))
            } catch (e: Exception) {
                UiState.Error(e.message)
            }
        }
    }

    fun selectCategory(category: Category?) {
        selectedCategory.value = category
    }
}
