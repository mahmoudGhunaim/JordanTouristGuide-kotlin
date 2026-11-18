package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.zu.jordantouristguide.repository.AuthError
import edu.zu.jordantouristguide.repository.AuthException
import edu.zu.jordantouristguide.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { LOGIN, REGISTER }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = false, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = false, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = false, error = null) }

    fun toggleMode() = _uiState.update {
        it.copy(
            mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
            nameError = false, emailError = false, passwordError = false, error = null,
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val isRegister = state.mode == AuthMode.REGISTER
        val nameError = isRegister && state.name.isBlank()
        val emailError = !isValidEmail(state.email)
        val passwordError = state.password.length < MIN_PASSWORD_LENGTH
        if (nameError || emailError || passwordError) {
            _uiState.update { it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                if (isRegister) {
                    authRepository.register(state.name, state.email, state.password)
                } else {
                    authRepository.login(state.email, state.password)
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: AuthException) {
                _uiState.update { it.copy(isLoading = false, error = e.error) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = AuthError.UNKNOWN) }
            }
        }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
    }
}
