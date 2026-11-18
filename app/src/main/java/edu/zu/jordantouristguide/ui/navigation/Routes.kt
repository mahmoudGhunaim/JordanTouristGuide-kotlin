package edu.zu.jordantouristguide.ui.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation Compose routes. Arguments are plain constructor parameters
// and can be read in a ViewModel with savedStateHandle["placeId"].

@Serializable data object SplashRoute
@Serializable data object OnboardingRoute

/** [returnAfterLogin] = true when opened from inside the app (e.g. "log in to review"). */
@Serializable data class AuthRoute(val returnAfterLogin: Boolean = false)

// Bottom-navigation tabs
@Serializable data object HomeRoute
@Serializable data object MapRoute
@Serializable data object FavoritesRoute
@Serializable data object ProfileRoute

@Serializable data class SearchRoute(val category: String? = null)
@Serializable data class DetailsRoute(val placeId: String)
@Serializable data class AddReviewRoute(val placeId: String)
