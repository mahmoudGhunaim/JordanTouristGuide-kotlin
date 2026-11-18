package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.model.User
import edu.zu.jordantouristguide.repository.AuthRepository
import edu.zu.jordantouristguide.repository.PlaceRepository
import edu.zu.jordantouristguide.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddReviewUiState(
    val placeName: String? = null,
    val placeNameAr: String? = null,
    val isCheckingUser: Boolean = true,
    /** The signed-in author, or null for guests. */
    val user: User? = null,
    val rating: Int = 0,
    val comment: String = "",
    val ratingError: Boolean = false,
    val commentError: Boolean = false,
    val isSubmitting: Boolean = false,
    val saveError: Boolean = false,
    val isSubmitted: Boolean = false,
)

@HiltViewModel
class AddReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    private val placeRepository: PlaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val placeId: String = checkNotNull(savedStateHandle["placeId"]) { "placeId is required" }

    private val _uiState = MutableStateFlow(AddReviewUiState())
    val uiState: StateFlow<AddReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user, isCheckingUser = false) }
            }
        }
        viewModelScope.launch {
            val place = runCatching { placeRepository.getPlace(placeId) }.getOrNull()
            _uiState.update { it.copy(placeName = place?.nameEn, placeNameAr = place?.nameAr) }
        }
    }

    fun onRatingChange(value: Int) = _uiState.update { it.copy(rating = value.coerceIn(1, 5), ratingError = false) }

    fun onCommentChange(value: String) = _uiState.update {
        it.copy(comment = value.take(MAX_COMMENT_LENGTH), commentError = false, saveError = false)
    }

    fun submit() {
        val state = _uiState.value
        val user = state.user ?: return // guests can't submit; the screen asks them to log in
        if (state.isSubmitting) return

        val ratingError = state.rating !in 1..5
        val commentError = state.comment.trim().length < MIN_COMMENT_LENGTH
        if (ratingError || commentError) {
            _uiState.update { it.copy(ratingError = ratingError, commentError = commentError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, saveError = false) }
        viewModelScope.launch {
            try {
                reviewRepository.addReview(placeId, user, state.rating, state.comment)
                _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, saveError = true) }
            }
        }
    }

    companion object {
        const val MIN_COMMENT_LENGTH = 3
        const val MAX_COMMENT_LENGTH = 500
    }
}
