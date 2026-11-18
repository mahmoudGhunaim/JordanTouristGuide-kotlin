package edu.zu.jordantouristguide.viewmodel

import androidx.lifecycle.SavedStateHandle
import edu.zu.jordantouristguide.testutil.FakeAuthRepository
import edu.zu.jordantouristguide.testutil.FakePlaceRepository
import edu.zu.jordantouristguide.testutil.FakeReviewRepository
import edu.zu.jordantouristguide.testutil.MainDispatcherRule
import edu.zu.jordantouristguide.testutil.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddReviewViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val reviews = FakeReviewRepository()

    private fun createViewModel(loggedIn: Boolean = true) = AddReviewViewModel(
        reviews,
        FakeAuthRepository(initialUser = if (loggedIn) TestData.user else null),
        FakePlaceRepository(),
        SavedStateHandle(mapOf("placeId" to "petra")),
    )

    @Test
    fun `guest has no user and cannot submit`() = runTest {
        val viewModel = createViewModel(loggedIn = false)
        assertFalse(viewModel.uiState.value.isCheckingUser)
        assertNull(viewModel.uiState.value.user)

        viewModel.onRatingChange(5)
        viewModel.onCommentChange("Great place")
        viewModel.submit()

        assertTrue(reviews.reviews.value.isEmpty())
    }

    @Test
    fun `missing rating and short comment show errors`() = runTest {
        val viewModel = createViewModel()

        viewModel.onCommentChange("ok")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertTrue(state.ratingError)
        assertTrue(state.commentError)
        assertFalse(state.isSubmitted)
    }

    @Test
    fun `valid review is saved with the user's name`() = runTest {
        val viewModel = createViewModel()
        assertEquals("Petra", viewModel.uiState.value.placeName)

        viewModel.onRatingChange(4)
        viewModel.onCommentChange("  Beautiful sunset  ")
        viewModel.submit()

        assertTrue(viewModel.uiState.value.isSubmitted)
        val saved = reviews.reviews.value.single()
        assertEquals("petra", saved.placeId)
        assertEquals(4, saved.rating)
        assertEquals("Beautiful sunset", saved.comment)
        assertEquals(TestData.user.name, saved.userName)
    }

    @Test
    fun `save failure shows an error`() = runTest {
        reviews.shouldFail = true
        val viewModel = createViewModel()

        viewModel.onRatingChange(5)
        viewModel.onCommentChange("Lovely")
        viewModel.submit()

        assertTrue(viewModel.uiState.value.saveError)
        assertFalse(viewModel.uiState.value.isSubmitted)
    }

    @Test
    fun `comment is limited to max length`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCommentChange("a".repeat(1000))
        assertEquals(AddReviewViewModel.MAX_COMMENT_LENGTH, viewModel.uiState.value.comment.length)
    }
}
