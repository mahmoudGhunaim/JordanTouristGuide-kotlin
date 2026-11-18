package edu.zu.jordantouristguide.repository

import com.google.firebase.firestore.FirebaseFirestore
import edu.zu.jordantouristguide.data.local.ReviewDao
import edu.zu.jordantouristguide.data.local.toEntity
import edu.zu.jordantouristguide.data.local.toModel
import edu.zu.jordantouristguide.data.model.Review
import edu.zu.jordantouristguide.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Reviews written by logged-in users. */
interface ReviewRepository {
    fun observeReviews(placeId: String): Flow<List<Review>>
    suspend fun addReview(placeId: String, author: User, rating: Int, comment: String)
}

/** Offline mode: reviews are saved in Room on this device. */
@Singleton
class LocalReviewRepository @Inject constructor(
    private val dao: ReviewDao,
) : ReviewRepository {

    override fun observeReviews(placeId: String): Flow<List<Review>> =
        dao.observeForPlace(placeId).map { list -> list.map { it.toModel() } }

    override suspend fun addReview(placeId: String, author: User, rating: Int, comment: String) {
        dao.insert(newReview(placeId, author, rating, comment).toEntity())
    }
}

/** Firebase mode: reviews live in the Firestore collection `reviews` and update in real time. */
@Singleton
class FirestoreReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ReviewRepository {

    override fun observeReviews(placeId: String): Flow<List<Review>> = callbackFlow {
        // Sorting is done on the device so no composite Firestore index is needed.
        val registration = firestore.collection(COLLECTION)
            .whereEqualTo("placeId", placeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents.orEmpty().map { doc ->
                    Review(
                        id = doc.id,
                        placeId = doc.getString("placeId").orEmpty(),
                        userId = doc.getString("userId").orEmpty(),
                        userName = doc.getString("userName").orEmpty(),
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment").orEmpty(),
                        timestamp = doc.getLong("timestamp") ?: 0L,
                    )
                }.sortedByDescending { it.timestamp }
                trySend(reviews)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addReview(placeId: String, author: User, rating: Int, comment: String) {
        val review = newReview(placeId, author, rating, comment)
        firestore.collection(COLLECTION).document(review.id).set(
            mapOf(
                "placeId" to review.placeId,
                "userId" to review.userId,
                "userName" to review.userName,
                "rating" to review.rating,
                "comment" to review.comment,
                "timestamp" to review.timestamp,
            ),
        ).await()
    }

    companion object {
        const val COLLECTION = "reviews"
    }
}

private fun newReview(placeId: String, author: User, rating: Int, comment: String) = Review(
    id = UUID.randomUUID().toString(),
    placeId = placeId,
    userId = author.id,
    userName = author.name,
    rating = rating.coerceIn(1, 5),
    comment = comment.trim(),
    timestamp = System.currentTimeMillis(),
)
