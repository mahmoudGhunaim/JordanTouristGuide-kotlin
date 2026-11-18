package edu.zu.jordantouristguide.data.model

/**
 * Pure filtering logic shared by Home and Search. Kept free of Android
 * code so it is easy to unit-test.
 */
object PlaceFilter {

    fun filter(
        places: List<Place>,
        query: String = "",
        category: Category? = null,
        governorate: Governorate? = null,
    ): List<Place> {
        val q = query.trim()
        return places.filter { place ->
            (category == null || place.category == category) &&
                (governorate == null || place.governorate == governorate) &&
                (q.isEmpty() ||
                    place.nameEn.contains(q, ignoreCase = true) ||
                    place.nameAr.contains(q) ||
                    place.governorate.key.contains(q, ignoreCase = true))
        }
    }

    fun topRated(places: List<Place>, limit: Int = 6): List<Place> =
        places.sortedByDescending { it.rating }.take(limit)
}
