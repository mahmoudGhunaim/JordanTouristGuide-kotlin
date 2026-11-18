package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.data.preferences.UserPreferences
import edu.zu.jordantouristguide.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Where the app goes after the splash screen. */
enum class StartDestination { ONBOARDING, AUTH, HOME }

/**
 * Decides the first real screen:
 * first launch -> Onboarding, logged in -> Home, otherwise -> Login.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<StartDestination?>(null)
    val destination: StateFlow<StartDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            _destination.value = when {
                !preferences.onboardingCompleted.first() -> StartDestination.ONBOARDING
                authRepository.currentUser.first() != null -> StartDestination.HOME
                else -> StartDestination.AUTH
            }
        }
    }
}

/** Saves that onboarding was shown, so it only appears the first time (DataStore). */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: UserPreferences,
) : ViewModel() {
    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            preferences.setOnboardingCompleted()
            onDone()
        }
    }
}
