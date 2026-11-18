package edu.zu.jordantouristguide.data.model

/** A user review of a place. [rating] is 1..5 stars; [timestamp] is epoch millis. */
data class Review(
    val id: String,
    val placeId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long,
)
