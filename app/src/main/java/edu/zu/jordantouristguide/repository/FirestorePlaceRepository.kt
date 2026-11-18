package edu.zu.jordantouristguide.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import edu.zu.jordantouristguide.config.AppConfig
import edu.zu.jordantouristguide.data.local.AssetReader
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.Governorate
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.data.model.TicketPrice
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads places from the Firestore collection `places`.
 * Each document has the same fields as one object in places.json.
 *
 * Firestore caches data on the device, so places that were loaded once
 * are still shown when the phone is offline.
 */
@Singleton
class FirestorePlaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val assetReader: AssetReader,
    private val json: Json,
) : PlaceRepository {

    private val mutex = Mutex()
    private var cache: List<Place>? = null

    override suspend fun getPlaces(forceRefresh: Boolean): List<Place> = mutex.withLock {
        val cached = cache
        if (cached != null && !forceRefresh) return cached

        var snapshot = firestore.collection(COLLECTION).get().await()
        if (snapshot.isEmpty && AppConfig.SEED_FIRESTORE_IF_EMPTY) {
            seedFromAssets()
            snapshot = firestore.collection(COLLECTION).get().await()
        }
        val places = snapshot.documents.mapNotNull { it.toPlace() }
        cache = places
        places
    }

    override suspend fun getPlace(id: String): Place? {
        cache?.firstOrNull { it.id == id }?.let { return it }
        return firestore.collection(COLLECTION).document(id).get().await().toPlace()
    }

    /** One-time upload of the bundled places.json into an empty Firestore project. */
    private suspend fun seedFromAssets() {
        val places = json.decodeFromString<List<Place>>(assetReader.readText(LocalPlaceRepository.FILE_NAME))
        val batch = firestore.batch()
        places.forEach { place ->
            batch.set(firestore.collection(COLLECTION).document(place.id), place.toFirestoreMap())
        }
        batch.commit().await()
    }

    companion object {
        const val COLLECTION = "places"
    }
}

/** Converts a Firestore document to a [Place]. Returns null for malformed documents. */
internal fun DocumentSnapshot.toPlace(): Place? {
    val category = Category.fromKey(getString("category")) ?: return null
    val governorate = Governorate.fromKey(getString("governorate")) ?: return null
    @Suppress("UNCHECKED_CAST")
    val ticket = get("ticketPrice") as? Map<String, Any?>
    @Suppress("UNCHECKED_CAST")
    val images = get("imageUrls") as? List<String>
    return Place(
        id = id,
        nameAr = getString("nameAr").orEmpty(),
        nameEn = getString("nameEn").orEmpty(),
        descriptionAr = getString("descriptionAr").orEmpty(),
        descriptionEn = getString("descriptionEn").orEmpty(),
        category = category,
        governorate = governorate,
        latitude = getDouble("latitude") ?: return null,
        longitude = getDouble("longitude") ?: return null,
        openingHours = getString("openingHours").orEmpty(),
        ticketPrice = TicketPrice(
            local = (ticket?.get("local") as? Number)?.toDouble() ?: 0.0,
            foreign = (ticket?.get("foreign") as? Number)?.toDouble(),
        ),
        rating = getDouble("rating") ?: 0.0,
        imageUrls = images.orEmpty(),
    )
}

private fun Place.toFirestoreMap(): Map<String, Any?> = mapOf(
    "nameAr" to nameAr,
    "nameEn" to nameEn,
    "descriptionAr" to descriptionAr,
    "descriptionEn" to descriptionEn,
    "category" to category.key,
    "governorate" to governorate.key,
    "latitude" to latitude,
    "longitude" to longitude,
    "openingHours" to openingHours,
    "ticketPrice" to mapOf("local" to ticketPrice.local, "foreign" to ticketPrice.foreign),
    "rating" to rating,
    "imageUrls" to imageUrls,
)
