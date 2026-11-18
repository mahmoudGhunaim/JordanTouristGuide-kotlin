package edu.zu.jordantouristguide.repository

import edu.zu.jordantouristguide.data.local.AssetReader
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.Governorate
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class LocalPlaceRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val sampleJson = """
        [
          {
            "id": "petra", "nameAr": "البتراء", "nameEn": "Petra",
            "descriptionAr": "وصف", "descriptionEn": "Description",
            "category": "Historical", "governorate": "Maan",
            "latitude": 30.3285, "longitude": 35.4444,
            "openingHours": "06:00 - 18:00",
            "ticketPrice": { "local": 1, "foreign": 50 },
            "rating": 4.9, "imageUrls": ["https://example.com/1.jpg"],
            "someFutureField": true
          },
          {
            "id": "aqaba", "nameAr": "العقبة", "nameEn": "Aqaba",
            "descriptionAr": "وصف", "descriptionEn": "Description",
            "category": "Entertainment", "governorate": "Aqaba",
            "latitude": 29.53, "longitude": 35.0,
            "openingHours": "24/7", "ticketPrice": { "local": 0 }, "rating": 4.5
          }
        ]
    """.trimIndent()

    private class CountingReader(private val text: String) : AssetReader {
        var reads = 0
        override fun readText(fileName: String): String {
            reads++
            return text
        }
    }

    @Test
    fun `parses places from json`() = runTest {
        val repo = LocalPlaceRepository(CountingReader(sampleJson), json, StandardTestDispatcher(testScheduler))

        val places = repo.getPlaces()

        assertEquals(2, places.size)
        val petra = places.first()
        assertEquals("Petra", petra.nameEn)
        assertEquals(Category.HISTORICAL, petra.category)
        assertEquals(Governorate.MAAN, petra.governorate)
        assertEquals(50.0, petra.ticketPrice.foreign!!, 0.0)
        assertTrue(places[1].ticketPrice.isFree)
        assertTrue(places[1].imageUrls.isEmpty())
    }

    @Test
    fun `getPlace returns matching place or null`() = runTest {
        val repo = LocalPlaceRepository(CountingReader(sampleJson), json, StandardTestDispatcher(testScheduler))

        assertEquals("Aqaba", repo.getPlace("aqaba")?.nameEn)
        assertNull(repo.getPlace("does-not-exist"))
    }

    @Test
    fun `file is read once and then cached`() = runTest {
        val reader = CountingReader(sampleJson)
        val repo = LocalPlaceRepository(reader, json, StandardTestDispatcher(testScheduler))

        repo.getPlaces()
        repo.getPlaces()
        repo.getPlace("petra")
        assertEquals(1, reader.reads)

        repo.getPlaces(forceRefresh = true)
        assertEquals(2, reader.reads)
    }

    @Test
    fun `invalid json throws so the UI can show an error state`() = runTest {
        val repo = LocalPlaceRepository(CountingReader("not json"), json, StandardTestDispatcher(testScheduler))
        try {
            repo.getPlaces()
            fail("Expected an exception")
        } catch (expected: Exception) {
            // ok
        }
    }

    /** Checks the real bundled seed file, so a typo in places.json fails the build. */
    @Test
    fun `bundled places json is valid`() = runTest {
        val file = File("src/main/assets/places.json")
        val repo = LocalPlaceRepository({ file.readText() }, json, StandardTestDispatcher(testScheduler))

        val places = repo.getPlaces()

        assertTrue("need at least 15 places", places.size >= 15)
        assertEquals("ids must be unique", places.size, places.map { it.id }.toSet().size)
        Category.entries.forEach { category ->
            assertTrue("missing category $category", places.any { it.category == category })
        }
        places.forEach { place ->
            assertTrue("${place.id} name", place.nameAr.isNotBlank() && place.nameEn.isNotBlank())
            assertTrue("${place.id} latitude inside Jordan", place.latitude in 29.0..33.5)
            assertTrue("${place.id} longitude inside Jordan", place.longitude in 34.8..39.5)
            assertTrue("${place.id} rating", place.rating in 0.0..5.0)
            assertTrue("${place.id} images", place.imageUrls.isNotEmpty())
        }
    }
}
