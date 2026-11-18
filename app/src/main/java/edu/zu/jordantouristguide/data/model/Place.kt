package edu.zu.jordantouristguide.data.model

import kotlinx.serialization.Serializable

/** A tourist place. Matches one object in assets/places.json and one Firestore document. */
@Serializable
data class Place(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val category: Category,
    val governorate: Governorate,
    val latitude: Double,
    val longitude: Double,
    val openingHours: String,
    val ticketPrice: TicketPrice,
    val rating: Double,
    val imageUrls: List<String> = emptyList(),
) {
    fun name(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
    fun description(isArabic: Boolean): String = if (isArabic) descriptionAr else descriptionEn
    val coverImage: String? get() = imageUrls.firstOrNull()
}

/**
 * Entry fee in Jordanian Dinars (JOD).
 * [foreign] is null when foreigners pay the same as locals.
 */
@Serializable
data class TicketPrice(
    val local: Double,
    val foreign: Double? = null,
) {
    val isFree: Boolean get() = local == 0.0 && (foreign == null || foreign == 0.0)
}
