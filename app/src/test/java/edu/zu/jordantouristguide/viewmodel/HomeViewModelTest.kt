package edu.zu.jordantouristguide.viewmodel

import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.testutil.FakeAuthRepository
import edu.zu.jordantouristguide.testutil.FakePlaceRepository
import edu.zu.jordantouristguide.testutil.MainDispatcherRule
import edu.zu.jordantouristguide.testutil.TestData
import edu.zu.jordantouristguide.testutil.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val placeRepository = FakePlaceRepository()
    private val authRepository = FakeAuthRepository()

    private fun createViewModel() = HomeViewModel(placeRepository, authRepository)

    @Test
    fun `loads places and top rated list`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        val content = (viewModel.uiState.value.content as UiState.Success).data
        assertEquals(TestData.places.size, content.places.size)
        assertEquals(TestData.petra, content.topRated.first())
        assertTrue(content.topRated.zipWithNext().all { (a, b) -> a.rating >= b.rating })
    }

    @Test
    fun `selecting a category filters all places`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        viewModel.selectCategory(Category.HISTORICAL)

        val state = viewModel.uiState.value
        assertEquals(Category.HISTORICAL, state.selectedCategory)
        val places = (state.content as UiState.Success).data.places
        assertEquals(setOf("petra", "jerash"), places.map { it.id }.toSet())
    }

    @Test
    fun `repository failure shows error and retry recovers`() = runTest {
        placeRepository.shouldFail = true
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)
        assertTrue(viewModel.uiState.value.content is UiState.Error)

        placeRepository.shouldFail = false
        viewModel.loadPlaces(forceRefresh = true)
        assertTrue(viewModel.uiState.value.content is UiState.Success)
    }

    @Test
    fun `greets the logged in user by name`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)
        assertNull(viewModel.uiState.value.userName)

        authRepository.login("mahmoud@example.com", "secret1")
        assertEquals(TestData.user.name, viewModel.uiState.value.userName)
    }
}
