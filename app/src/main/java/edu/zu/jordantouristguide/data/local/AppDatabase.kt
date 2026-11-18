package edu.zu.jordantouristguide.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** The app's local SQLite database (Room). Created in di/DataModule.kt. */
@Database(
    entities = [FavoriteEntity::class, ReviewEntity::class, LocalUserEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun reviewDao(): ReviewDao
    abstract fun localUserDao(): LocalUserDao

    companion object {
        const val NAME = "jordan_guide.db"
    }
}
