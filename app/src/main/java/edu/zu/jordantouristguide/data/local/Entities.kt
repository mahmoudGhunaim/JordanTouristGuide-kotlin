package edu.zu.jordantouristguide.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.FavoritePlace
import edu.zu.jordantouristguide.data.model.Governorate
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.data.model.Review
import edu.zu.jordantouristguide.data.model.User

/** Room table for favorites. Stores a snapshot of the place so it works offline. */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val placeId: String,
    val nameAr: String,
    val nameEn: String,
    val category: String,
    val governorate: String,
    val rating: Double,
    val imageUrl: String?,
    val savedAt: Long,
)

fun FavoriteEntity.toModel() = FavoritePlace(
    placeId = placeId,
    nameAr = nameAr,
    nameEn = nameEn,
    category = Category.fromKey(category) ?: Category.HISTORICAL,
    governorate = Governorate.fromKey(governorate) ?: Governorate.AMMAN,
    rating = rating,
    imageUrl = imageUrl,
    savedAt = savedAt,
)

fun Place.toFavoriteEntity(savedAt: Long) = FavoriteEntity(
    placeId = id,
    nameAr = nameAr,
    nameEn = nameEn,
    category = category.key,
    governorate = governorate.key,
    rating = rating,
    imageUrl = coverImage,
    savedAt = savedAt,
)

/** Room table for reviews in offline mode (Firestore is used when Firebase is on). */
@Entity(tableName = "reviews", indices = [Index("placeId")])
data class ReviewEntity(
    @PrimaryKey val id: String,
    val placeId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long,
)

fun ReviewEntity.toModel() = Review(id, placeId, userId, userName, rating, comment, timestamp)

fun Review.toEntity() = ReviewEntity(id, placeId, userId, userName, rating, comment, timestamp)

/**
 * Demo accounts for offline mode only, so Login/Register can be tried
 * without Firebase. Passwords are stored as a SHA-256 hash, never as plain text.
 */
@Entity(tableName = "local_users", indices = [Index(value = ["email"], unique = true)])
data class LocalUserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
)

fun LocalUserEntity.toModel() = User(id = id, name = name, email = email)
