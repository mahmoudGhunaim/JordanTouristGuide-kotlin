package edu.zu.jordantouristguide.data.model

import edu.zu.jordantouristguide.testutil.TestData
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceFilterTest {

    private val places = TestData.places

    @Test
    fun `empty filters return everything`() {
        assertEquals(places, PlaceFilter.filter(places))
    }

    @Test
    fun `query matches English name ignoring case`() {
        assertEquals(listOf(TestData.petra), PlaceFilter.filter(places, query = "  peTRA "))
    }

    @Test
    fun `query matches Arabic name`() {
        assertEquals(listOf(TestData.wadiRum), PlaceFilter.filter(places, query = "رم"))
    }

    @Test
    fun `category and governorate combine`() {
        val result = PlaceFilter.filter(places, category = Category.HISTORICAL, governorate = Governorate.JERASH)
        assertEquals(listOf(TestData.jerash), result)
    }

    @Test
    fun `top rated is sorted by rating and limited`() {
        val top = PlaceFilter.topRated(places, limit = 2)
        assertEquals(listOf(TestData.petra, TestData.wadiRum), top)
    }
}
