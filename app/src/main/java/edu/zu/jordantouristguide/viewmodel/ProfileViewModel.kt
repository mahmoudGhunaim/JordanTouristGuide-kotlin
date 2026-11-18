package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.model.ThemeMode
import edu.zu.jordantouristguide.data.model.User
import edu.zu.jordantouristguide.data.preferences.UserPreferences
import edu.zu.jordantouristguide.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

/**
 * Profile & settings. The language switch is done in the UI layer
 * (AppCompatDelegate) because it is an Android framework call.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferences: UserPreferences,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(authRepository.currentUser, preferences.themeMode) { user, theme ->
        ProfileUiState(isLoading = false, user = user, themeMode = theme)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
