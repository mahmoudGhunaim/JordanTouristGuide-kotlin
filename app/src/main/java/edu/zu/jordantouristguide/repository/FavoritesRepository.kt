package edu.zu.jordantouristguide.repository

import edu.zu.jordantouristguide.data.local.FavoriteDao
import edu.zu.jordantouristguide.data.local.toFavoriteEntity
import edu.zu.jordantouristguide.data.local.toModel
import edu.zu.jordantouristguide.data.model.FavoritePlace
import edu.zu.jordantouristguide.data.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Favorites are always stored locally in Room, so they work offline. */
interface FavoritesRepository {
    fun observeFavorites(): Flow<List<FavoritePlace>>
    fun observeIsFavorite(placeId: String): Flow<Boolean>
    suspend fun addFavorite(place: Place)
    suspend fun removeFavorite(placeId: String)
}

@Singleton
class RoomFavoritesRepository @Inject constructor(
    private val dao: FavoriteDao,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<FavoritePlace>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeIsFavorite(placeId: String): Flow<Boolean> = dao.observeIsFavorite(placeId)

    override suspend fun addFavorite(place: Place) {
        dao.insert(place.toFavoriteEntity(savedAt = System.currentTimeMillis()))
    }

    override suspend fun removeFavorite(placeId: String) = dao.delete(placeId)
}
