package edu.zu.jordantouristguide.viewmodel

import edu.zu.jordantouristguide.data.model.ThemeMode
import edu.zu.jordantouristguide.testutil.FakeAuthRepository
import edu.zu.jordantouristguide.testutil.FakeFavoritesRepository
import edu.zu.jordantouristguide.testutil.FakePlaceRepository
import edu.zu.jordantouristguide.testutil.FakeUserPreferences
import edu.zu.jordantouristguide.testutil.MainDispatcherRule
import edu.zu.jordantouristguide.testutil.TestData
import edu.zu.jordantouristguide.testutil.keepCollecting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `shows saved favorites and removes them`() = runTest {
        val repo = FakeFavoritesRepository()
        repo.addFavorite(TestData.petra)
        repo.addFavorite(TestData.jerash)
        val viewModel = FavoritesViewModel(repo)
        keepCollecting(viewModel.uiState)

        val names = (viewModel.uiState.value as UiState.Success).data.map { it.nameEn }
        assertEquals(listOf("Jerash", "Petra"), names) // newest first

        viewModel.removeFavorite("petra")
        assertEquals(listOf("jerash"), (viewModel.uiState.value as UiState.Success).data.map { it.placeId })
    }

    @Test
    fun `empty list when nothing is saved`() = runTest {
        val viewModel = FavoritesViewModel(FakeFavoritesRepository())
        keepCollecting(viewModel.uiState)
        assertTrue((viewModel.uiState.value as UiState.Success).data.isEmpty())
    }
}

class MapViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads markers and selects a place`() = runTest {
        val viewModel = MapViewModel(FakePlaceRepository())
        assertEquals(5, (viewModel.uiState.value.places as UiState.Success).data.size)

        viewModel.selectPlace(TestData.petra)
        assertEquals(TestData.petra, viewModel.uiState.value.selectedPlace)

        viewModel.selectPlace(null)
        assertNull(viewModel.uiState.value.selectedPlace)
    }

    @Test
    fun `error state when places fail to load`() = runTest {
        val viewModel = MapViewModel(FakePlaceRepository(shouldFail = true))
        assertTrue(viewModel.uiState.value.places is UiState.Error)
    }
}

class SplashViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `first launch goes to onboarding`() = runTest {
        val viewModel = SplashViewModel(FakeUserPreferences(onboardingDone = false), FakeAuthRepository())
        assertEquals(StartDestination.ONBOARDING, viewModel.destination.value)
    }

    @Test
    fun `returning guest goes to login`() = runTest {
        val viewModel = SplashViewModel(FakeUserPreferences(onboardingDone = true), FakeAuthRepository())
        assertEquals(StartDestination.AUTH, viewModel.destination.value)
    }

    @Test
    fun `logged in user goes straight home`() = runTest {
        val viewModel = SplashViewModel(
            FakeUserPreferences(onboardingDone = true),
            FakeAuthRepository(initialUser = TestData.user),
        )
        assertEquals(StartDestination.HOME, viewModel.destination.value)
    }

    @Test
    fun `completing onboarding is saved`() = runTest {
        val prefs = FakeUserPreferences()
        var done = false
        OnboardingViewModel(prefs).completeOnboarding { done = true }
        assertTrue(done)
        assertTrue(prefs.onboardingCompleted.first())
    }
}

class ProfileViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `changes theme and logs out`() = runTest {
        val prefs = FakeUserPreferences()
        val auth = FakeAuthRepository(initialUser = TestData.user)
        val viewModel = ProfileViewModel(auth, prefs)
        keepCollecting(viewModel.uiState)
        assertEquals(TestData.user, viewModel.uiState.value.user)

        viewModel.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)

        viewModel.logout()
        assertNull(viewModel.uiState.value.user)
    }
}
