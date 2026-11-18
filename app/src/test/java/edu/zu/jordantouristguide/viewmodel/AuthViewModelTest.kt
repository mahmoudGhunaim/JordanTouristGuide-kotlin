package edu.zu.jordantouristguide.viewmodel

import edu.zu.jordantouristguide.repository.AuthError
import edu.zu.jordantouristguide.testutil.FakeAuthRepository
import edu.zu.jordantouristguide.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val viewModel = AuthViewModel(auth)

    @Test
    fun `invalid email and short password are rejected before calling the repository`() = runTest {
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("123")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertTrue(state.emailError)
        assertTrue(state.passwordError)
        assertFalse(state.isSuccess)
        assertNull(auth.lastLoginEmail)
    }

    @Test
    fun `successful login`() = runTest {
        viewModel.onEmailChange("mahmoud@example.com")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals("mahmoud@example.com", auth.lastLoginEmail)
    }

    @Test
    fun `register requires a name`() = runTest {
        viewModel.toggleMode()
        viewModel.onEmailChange("ahmed@example.com")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        assertTrue(viewModel.uiState.value.nameError)

        viewModel.onNameChange("Ahmed")
        viewModel.submit()
        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals("Ahmed", auth.lastRegisterName)
    }

    @Test
    fun `repository error is shown to the user`() = runTest {
        auth.nextError = AuthError.INVALID_CREDENTIALS
        viewModel.onEmailChange("mahmoud@example.com")
        viewModel.onPasswordChange("wrong-password")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertEquals(AuthError.INVALID_CREDENTIALS, state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `email validation`() {
        assertTrue(AuthViewModel.isValidEmail("student@zu.edu.jo"))
        assertFalse(AuthViewModel.isValidEmail("student@zu"))
        assertFalse(AuthViewModel.isValidEmail("@zu.edu.jo"))
    }
}
