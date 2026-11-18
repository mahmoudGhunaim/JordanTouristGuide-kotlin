package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.SavedStateHandle
import edu.zu.jordantouristguide.testutil.FakeAuthRepository
import edu.zu.jordantouristguide.testutil.FakeFavoritesRepository
import edu.zu.jordantouristguide.testutil.FakePlaceRepository
import edu.zu.jordantouristguide.testutil.FakeReviewRepository
import edu.zu.jordantouristguide.testutil.MainDispatcherRule
import edu.zu.jordantouristguide.testutil.TestData
import edu.zu.jordantouristguide.testutil.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlaceDetailsViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val placeRepository = FakePlaceRepository()
    private val favorites = FakeFavoritesRepository()
    private val reviews = FakeReviewRepository()
    private val auth = FakeAuthRepository()

    private fun createViewModel(placeId: String = "petra") = PlaceDetailsViewModel(
        placeRepository, favorites, reviews, auth, SavedStateHandle(mapOf("placeId" to placeId)),
    )

    @Test
    fun `loads the place from the navigation argument`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        val state = viewModel.uiState.value
        assertEquals(TestData.petra, (state.place as UiState.Success).data)
        assertEquals(emptyList<Any>(), state.reviews)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun `unknown id shows not found`() = runTest {
        val viewModel = createViewModel(placeId = "atlantis")
        keepCollecting(viewModel.uiState)

        assertTrue(viewModel.uiState.value.isNotFound)
    }

    @Test
    fun `network error shows error state`() = runTest {
        placeRepository.shouldFail = true
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        val state = viewModel.uiState.value
        assertTrue(state.place is UiState.Error)
        assertFalse(state.isNotFound)
    }

    @Test
    fun `toggle favorite adds then removes`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        viewModel.toggleFavorite()
        assertTrue(viewModel.uiState.value.isFavorite)
        assertEquals(setOf("petra"), favorites.ids)

        viewModel.toggleFavorite()
        assertFalse(viewModel.uiState.value.isFavorite)
        assertTrue(favorites.ids.isEmpty())
    }

    @Test
    fun `shows reviews for this place only`() = runTest {
        reviews.addReview("petra", TestData.user, 5, "Amazing")
        reviews.addReview("jerash", TestData.user, 4, "Nice")
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        assertEquals(listOf("Amazing"), viewModel.uiState.value.reviews?.map { it.comment })
    }

    @Test
    fun `knows when the user is logged in`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        auth.login("mahmoud@example.com", "secret1")

        assertTrue(viewModel.uiState.value.isLoggedIn)
    }
}
