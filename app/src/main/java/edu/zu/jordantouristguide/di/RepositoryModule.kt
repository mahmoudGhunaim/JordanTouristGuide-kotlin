package edu.zu.jordantouristguide.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.zu.jordantouristguide.config.AppConfig
import edu.zu.jordantouristguide.repository.AuthRepository
import edu.zu.jordantouristguide.repository.FavoritesRepository
import edu.zu.jordantouristguide.repository.FirebaseAuthRepository
import edu.zu.jordantouristguide.repository.FirestorePlaceRepository
import edu.zu.jordantouristguide.repository.FirestoreReviewRepository
import edu.zu.jordantouristguide.repository.LocalAuthRepository
import edu.zu.jordantouristguide.repository.LocalPlaceRepository
import edu.zu.jordantouristguide.repository.LocalReviewRepository
import edu.zu.jordantouristguide.repository.PlaceRepository
import edu.zu.jordantouristguide.repository.ReviewRepository
import edu.zu.jordantouristguide.repository.RoomFavoritesRepository
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Chooses which repository implementations the app uses.
 * This is the ONLY place that knows about the local/Firebase switch.
 * [Provider] creates an implementation only when it is actually chosen.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePlaceRepository(
        local: Provider<LocalPlaceRepository>,
        firestore: Provider<FirestorePlaceRepository>,
    ): PlaceRepository = if (AppConfig.isFirebaseEnabled) firestore.get() else local.get()

    @Provides
    @Singleton
    fun provideReviewRepository(
        local: Provider<LocalReviewRepository>,
        firestore: Provider<FirestoreReviewRepository>,
    ): ReviewRepository = if (AppConfig.isFirebaseEnabled) firestore.get() else local.get()

    @Provides
    @Singleton
    fun provideAuthRepository(
        local: Provider<LocalAuthRepository>,
        firebase: Provider<FirebaseAuthRepository>,
    ): AuthRepository = if (AppConfig.isFirebaseEnabled) firebase.get() else local.get()

    @Provides
    @Singleton
    fun provideFavoritesRepository(impl: RoomFavoritesRepository): FavoritesRepository = impl
}
