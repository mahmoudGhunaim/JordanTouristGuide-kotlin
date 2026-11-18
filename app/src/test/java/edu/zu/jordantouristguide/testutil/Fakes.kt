package edu.zu.jordantouristguide.testutil

import edu.zu.jordantouristguide.data.local.LocalUserDao
import edu.zu.jordantouristguide.data.local.LocalUserEntity
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.FavoritePlace
import edu.zu.jordantouristguide.data.model.Governorate
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.data.model.Review
import edu.zu.jordantouristguide.data.model.ThemeMode
import edu.zu.jordantouristguide.data.model.TicketPrice
import edu.zu.jordantouristguide.data.model.User
import edu.zu.jordantouristguide.data.preferences.UserPreferences
import edu.zu.jordantouristguide.repository.AuthError
import edu.zu.jordantouristguide.repository.AuthException
import edu.zu.jordantouristguide.repository.AuthRepository
import edu.zu.jordantouristguide.repository.FavoritesRepository
import edu.zu.jordantouristguide.repository.PlaceRepository
import edu.zu.jordantouristguide.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// Simple in-memory fakes. They are easier to read than mocks and behave like the real thing.

object TestData {
    fun place(
        id: String,
        nameEn: String = id.replaceFirstChar { it.uppercase() },
        nameAr: String = "مكان $id",
        category: Category = Category.HISTORICAL,
        governorate: Governorate = Governorate.AMMAN,
        rating: Double = 4.0,
    ) = Place(
        id = id,
        nameAr = nameAr,
        nameEn = nameEn,
        descriptionAr = "وصف",
        descriptionEn = "Description",
        category = category,
        governorate = governorate,
        latitude = 31.0,
        longitude = 35.9,
        openingHours = "08:00 - 18:00",
        ticketPrice = TicketPrice(local = 1.0, foreign = 10.0),
        rating = rating,
        imageUrls = listOf("https://example.com/$id.jpg"),
    )

    val petra = place("petra", "Petra", "البتراء", Category.HISTORICAL, Governorate.MAAN, 4.9)
    val wadiRum = place("wadi-rum", "Wadi Rum", "وادي رم", Category.NATURE, Governorate.AQABA, 4.8)
    val jerash = place("jerash", "Jerash", "جرش", Category.HISTORICAL, Governorate.JERASH, 4.7)
    val nebo = place("mount-nebo", "Mount Nebo", "جبل نيبو", Category.RELIGIOUS, Governorate.MADABA, 4.6)
    val hashem = place("hashem", "Hashem Restaurant", "مطعم هاشم", Category.RESTAURANTS, Governorate.AMMAN, 4.5)

    val places = listOf(hashem, jerash, petra, nebo, wadiRum)

    val user = User(id = "u1", name = "Mahmoud", email = "mahmoud@example.com")
}

class FakePlaceRepository(
    var places: List<Place> = TestData.places,
    var shouldFail: Boolean = false,
) : PlaceRepository {
    var getPlacesCalls = 0

    override suspend fun getPlaces(forceRefresh: Boolean): List<Place> {
        getPlacesCalls++
        if (shouldFail) error("Network down")
        return places
    }

    override suspend fun getPlace(id: String): Place? {
        if (shouldFail) error("Network down")
        return places.firstOrNull { it.id == id }
    }
}

class FakeFavoritesRepository : FavoritesRepository {
    private val favorites = MutableStateFlow<Map<String, FavoritePlace>>(emptyMap())

    override fun observeFavorites(): Flow<List<FavoritePlace>> =
        favorites.map { it.values.sortedByDescending { fav -> fav.savedAt } }

    override fun observeIsFavorite(placeId: String): Flow<Boolean> = favorites.map { placeId in it }

    override suspend fun addFavorite(place: Place) {
        val fav = FavoritePlace(
            placeId = place.id,
            nameAr = place.nameAr,
            nameEn = place.nameEn,
            category = place.category,
            governorate = place.governorate,
            rating = place.rating,
            imageUrl = place.coverImage,
            savedAt = favorites.value.size.toLong(),
        )
        favorites.value = favorites.value + (place.id to fav)
    }

    override suspend fun removeFavorite(placeId: String) {
        favorites.value = favorites.value - placeId
    }

    val ids: Set<String> get() = favorites.value.keys
}

class FakeReviewRepository(var shouldFail: Boolean = false) : ReviewRepository {
    val reviews = MutableStateFlow<List<Review>>(emptyList())

    override fun observeReviews(placeId: String): Flow<List<Review>> =
        reviews.map { list -> list.filter { it.placeId == placeId } }

    override suspend fun addReview(placeId: String, author: User, rating: Int, comment: String) {
        if (shouldFail) error("Write failed")
        reviews.value = reviews.value + Review(
            id = "r${reviews.value.size + 1}",
            placeId = placeId,
            userId = author.id,
            userName = author.name,
            rating = rating,
            comment = comment.trim(),
            timestamp = reviews.value.size.toLong(),
        )
    }
}

class FakeAuthRepository(initialUser: User? = null) : AuthRepository {
    private val user = MutableStateFlow(initialUser)
    override val currentUser: Flow<User?> = user

    /** When set, login/register throw this error. */
    var nextError: AuthError? = null
    var lastLoginEmail: String? = null
    var lastRegisterName: String? = null

    override suspend fun login(email: String, password: String): User {
        nextError?.let { throw AuthException(it) }
        lastLoginEmail = email
        return TestData.user.copy(email = email).also { user.value = it }
    }

    override suspend fun register(name: String, email: String, password: String): User {
        nextError?.let { throw AuthException(it) }
        lastRegisterName = name
        return User("new", name, email).also { user.value = it }
    }

    override suspend fun logout() {
        user.value = null
    }
}

class FakeUserPreferences(onboardingDone: Boolean = false) : UserPreferences {
    private val onboarding = MutableStateFlow(onboardingDone)
    private val theme = MutableStateFlow(ThemeMode.SYSTEM)
    private val session = MutableStateFlow<String?>(null)

    override val onboardingCompleted: Flow<Boolean> = onboarding
    override val themeMode: Flow<ThemeMode> = theme
    override val localSessionUserId: Flow<String?> = session

    override suspend fun setOnboardingCompleted() { onboarding.value = true }
    override suspend fun setThemeMode(mode: ThemeMode) { theme.value = mode }
    override suspend fun setLocalSessionUserId(userId: String?) { session.value = userId }
}

class FakeLocalUserDao : LocalUserDao {
    private val users = mutableMapOf<String, LocalUserEntity>()
    override suspend fun findByEmail(email: String) = users.values.firstOrNull { it.email == email }
    override suspend fun findById(id: String) = users[id]
    override suspend fun insert(user: LocalUserEntity) {
        users[user.id] = user
    }
}
