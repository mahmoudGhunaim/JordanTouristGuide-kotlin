package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val places: UiState<List<Place>> = UiState.Loading,
    /** The place whose marker was tapped (shown in a small card). */
    val selectedPlace: Place? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(places = UiState.Loading) }
            val result = try {
                UiState.Success(placeRepository.getPlaces())
            } catch (e: Exception) {
                UiState.Error(e.message)
            }
            _uiState.update { it.copy(places = result) }
        }
    }

    fun selectPlace(place: Place?) = _uiState.update { it.copy(selectedPlace = place) }
}
