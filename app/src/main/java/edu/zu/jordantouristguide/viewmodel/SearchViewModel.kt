package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.Governorate
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.data.model.PlaceFilter
import edu.zu.jordantouristguide.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val category: Category? = null,
    val governorate: Governorate? = null,
    /** Governorates that actually have places, for the filter menu. */
    val governorates: List<Governorate> = emptyList(),
    val results: UiState<List<Place>> = UiState.Loading,
) {
    val hasFilters: Boolean get() = query.isNotBlank() || category != null || governorate != null
}

/** Search by name + filter by category and governorate. */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val allPlaces = MutableStateFlow<UiState<List<Place>>>(UiState.Loading)
    private val query = MutableStateFlow("")
    // Pre-selected when coming from a category on the Home screen (SearchRoute.category).
    private val category = MutableStateFlow(Category.fromKey(savedStateHandle.get<String>("category")))
    private val governorate = MutableStateFlow<Governorate?>(null)

    val uiState: StateFlow<SearchUiState> = combine(allPlaces, query, category, governorate) { places, q, c, g ->
        SearchUiState(
            query = q,
            category = c,
            governorate = g,
            governorates = (places as? UiState.Success)?.data
                ?.map { it.governorate }?.distinct()?.sortedBy { it.ordinal }
                .orEmpty(),
            results = when (places) {
                is UiState.Success -> UiState.Success(
                    PlaceFilter.filter(places.data, q, c, g).sortedByDescending { it.rating },
                )
                else -> places
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState(category = category.value))

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            allPlaces.value = UiState.Loading
            allPlaces.value = try {
                UiState.Success(placeRepository.getPlaces())
            } catch (e: Exception) {
                UiState.Error(e.message)
            }
        }
    }

    fun onQueryChange(value: String) { query.value = value }
    fun onCategoryChange(value: Category?) { category.value = value }
    fun onGovernorateChange(value: Governorate?) { governorate.value = value }

    fun resetFilters() {
        query.value = ""
        category.value = null
        governorate.value = null
    }
}
