package edu.zu.jordantouristguide.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.zu.jordantouristguide.data.local.AndroidAssetReader
import edu.zu.jordantouristguide.data.local.AppDatabase
import edu.zu.jordantouristguide.data.local.AssetReader
import edu.zu.jordantouristguide.data.local.FavoriteDao
import edu.zu.jordantouristguide.data.local.LocalUserDao
import edu.zu.jordantouristguide.data.local.ReviewDao
import edu.zu.jordantouristguide.data.preferences.DataStoreUserPreferences
import edu.zu.jordantouristguide.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

/** Marks the dispatcher used for disk and network work. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Provides the data-layer building blocks: database, DataStore, JSON parser, Firebase SDKs. */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideReviewDao(db: AppDatabase): ReviewDao = db.reviewDao()
    @Provides fun provideLocalUserDao(db: AppDatabase): LocalUserDao = db.localUserDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    // These are only requested when AppConfig.isFirebaseEnabled is true (see RepositoryModule),
    // so the app never touches Firebase before google-services.json is added.
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds abstract fun bindAssetReader(impl: AndroidAssetReader): AssetReader
    @Binds abstract fun bindUserPreferences(impl: DataStoreUserPreferences): UserPreferences
}
