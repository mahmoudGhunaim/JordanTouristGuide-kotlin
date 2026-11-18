package edu.zu.jordantouristguide.data.model

/**
 * A saved favorite. It keeps a small copy of the place so the Favorites
 * screen works fully offline (even when places normally come from Firestore).
 */
data class FavoritePlace(
    val placeId: String,
    val nameAr: String,
    val nameEn: String,
    val category: Category,
    val governorate: Governorate,
    val rating: Double,
    val imageUrl: String?,
    val savedAt: Long,
) {
    fun name(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
}
