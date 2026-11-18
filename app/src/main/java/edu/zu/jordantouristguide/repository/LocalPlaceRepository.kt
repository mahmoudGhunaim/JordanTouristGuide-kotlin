package edu.zu.jordantouristguide.repository

import edu.zu.jordantouristguide.data.local.AssetReader
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default, offline implementation: reads `assets/places.json` once and keeps
 * the parsed list in memory.
 */
@Singleton
class LocalPlaceRepository @Inject constructor(
    private val assetReader: AssetReader,
    private val json: Json,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PlaceRepository {

    private val mutex = Mutex()
    private var cache: List<Place>? = null

    override suspend fun getPlaces(forceRefresh: Boolean): List<Place> = mutex.withLock {
        val cached = cache
        if (cached != null && !forceRefresh) return cached

        val places = withContext(ioDispatcher) {
            json.decodeFromString<List<Place>>(assetReader.readText(FILE_NAME))
        }
        cache = places
        places
    }

    override suspend fun getPlace(id: String): Place? = getPlaces().firstOrNull { it.id == id }

    companion object {
        const val FILE_NAME = "places.json"
    }
}
