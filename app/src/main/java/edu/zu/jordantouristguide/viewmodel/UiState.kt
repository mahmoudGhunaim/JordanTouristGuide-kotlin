package edu.zu.jordantouristguide.viewmodel

/**
 * Generic screen state used by ViewModels that load data.
 * Every screen shows a spinner for [Loading], an error view with "Try again"
 * for [Error], and its content (or an empty state) for [Success].
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String? = null) : UiState<Nothing>
}
