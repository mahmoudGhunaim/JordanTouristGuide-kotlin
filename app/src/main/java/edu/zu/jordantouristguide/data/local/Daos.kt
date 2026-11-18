package edu.zu.jordantouristguide.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE placeId = :placeId)")
    fun observeIsFavorite(placeId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE placeId = :placeId")
    suspend fun delete(placeId: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE placeId = :placeId ORDER BY timestamp DESC")
    fun observeForPlace(placeId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewEntity)
}

@Dao
interface LocalUserDao {
    @Query("SELECT * FROM local_users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): LocalUserEntity?

    @Query("SELECT * FROM local_users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): LocalUserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: LocalUserEntity)
}
