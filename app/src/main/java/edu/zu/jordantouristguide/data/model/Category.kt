package edu.zu.jordantouristguide.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Place categories. [SerialName] is the value stored in places.json / Firestore. */
@Serializable
enum class Category {
    @SerialName("Historical") HISTORICAL,
    @SerialName("Nature") NATURE,
    @SerialName("Religious") RELIGIOUS,
    @SerialName("Entertainment") ENTERTAINMENT,
    @SerialName("Restaurants") RESTAURANTS;

    /** The value written to places.json and Firestore. */
    val key: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }

    companion object {
        fun fromKey(key: String?): Category? = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}
