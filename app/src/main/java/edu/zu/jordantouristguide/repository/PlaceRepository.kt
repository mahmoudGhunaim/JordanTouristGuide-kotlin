package edu.zu.jordantouristguide.repository

import edu.zu.jordantouristguide.data.model.Place

/**
 * Source of tourist places. ViewModels only depend on this interface, so the
 * data can come from the bundled JSON file ([LocalPlaceRepository]) or from
 * Cloud Firestore ([FirestorePlaceRepository]) without changing any UI code.
 * The implementation is chosen in di/RepositoryModule.kt using AppConfig.
 */
interface PlaceRepository {
    /** All places. Throws an exception if the data cannot be loaded. */
    suspend fun getPlaces(forceRefresh: Boolean = false): List<Place>

    /** A single place, or null if no place has this id. */
    suspend fun getPlace(id: String): Place?
}
