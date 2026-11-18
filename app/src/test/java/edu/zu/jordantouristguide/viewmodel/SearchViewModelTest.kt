package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.SavedStateHandle
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.Governorate
import edu.zu.jordantouristguide.testutil.FakePlaceRepository
import edu.zu.jordantouristguide.testutil.MainDispatcherRule
import edu.zu.jordantouristguide.testutil.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(category: String? = null) =
        SearchViewModel(FakePlaceRepository(), SavedStateHandle(mapOf("category" to category)))

    private fun SearchViewModel.resultIds() =
        (uiState.value.results as UiState.Success).data.map { it.id }

    @Test
    fun `search by name`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        viewModel.onQueryChange("jer")

        assertEquals(listOf("jerash"), viewModel.resultIds())
    }

    @Test
    fun `filter by governorate`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        viewModel.onGovernorateChange(Governorate.AMMAN)

        assertEquals(listOf("hashem"), viewModel.resultIds())
    }

    @Test
    fun `category from navigation argument is preselected`() = runTest {
        val viewModel = createViewModel(category = "Nature")
        keepCollecting(viewModel.uiState)

        assertEquals(Category.NATURE, viewModel.uiState.value.category)
        assertEquals(listOf("wadi-rum"), viewModel.resultIds())
    }

    @Test
    fun `results are sorted by rating and empty search shows empty list`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)
        assertEquals("petra", viewModel.resultIds().first())

        viewModel.onQueryChange("zzz")
        assertTrue(viewModel.resultIds().isEmpty())
        assertTrue(viewModel.uiState.value.hasFilters)
    }

    @Test
    fun `reset clears all filters`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)
        viewModel.onQueryChange("petra")
        viewModel.onCategoryChange(Category.NATURE)

        viewModel.resetFilters()

        assertFalse(viewModel.uiState.value.hasFilters)
        assertEquals(5, viewModel.resultIds().size)
    }

    @Test
    fun `governorate menu only lists governorates that have places`() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.uiState)

        val governorates = viewModel.uiState.value.governorates
        assertTrue(Governorate.AMMAN in governorates)
        assertFalse(Governorate.ZARQA in governorates)
    }
}
