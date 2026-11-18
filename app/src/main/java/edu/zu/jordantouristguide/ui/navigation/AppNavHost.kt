package edu.zu.jordantouristguide.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import edu.zu.jordantouristguide.ui.screens.auth.AuthScreen
import edu.zu.jordantouristguide.ui.screens.details.PlaceDetailsScreen
import edu.zu.jordantouristguide.ui.screens.favorites.FavoritesScreen
import edu.zu.jordantouristguide.ui.screens.home.HomeScreen
import edu.zu.jordantouristguide.ui.screens.map.MapScreen
import edu.zu.jordantouristguide.ui.screens.onboarding.OnboardingScreen
import edu.zu.jordantouristguide.ui.screens.profile.ProfileScreen
import edu.zu.jordantouristguide.ui.screens.review.AddReviewScreen
import edu.zu.jordantouristguide.ui.screens.search.SearchScreen
import edu.zu.jordantouristguide.ui.screens.splash.SplashScreen
import edu.zu.jordantouristguide.ui.util.SystemBarIconsEffect
import edu.zu.jordantouristguide.viewmodel.StartDestination

/**
 * The whole navigation graph of the single-activity app.
 * The bottom bar is only shown on the four top-level tabs.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Screens that draw a photo or the rose header behind the status bar need white icons.
    val drawsBehindStatusBar = listOf(SplashRoute::class, HomeRoute::class, DetailsRoute::class)
        .any { currentDestination?.hasRoute(it) == true }
    SystemBarIconsEffect(lightIcons = drawsBehindStatusBar)

    Scaffold(
        // Each screen handles its own status-bar insets.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentDestination.isTopLevel()) {
                AppBottomBar(currentDestination) { tab -> navController.navigateToTab(tab) }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable<SplashRoute> {
                SplashScreen(onFinished = { destination ->
                    val route: Any = when (destination) {
                        StartDestination.ONBOARDING -> OnboardingRoute
                        StartDestination.AUTH -> AuthRoute()
                        StartDestination.HOME -> HomeRoute
                    }
                    navController.navigate(route) { popUpTo(SplashRoute) { inclusive = true } }
                })
            }

            composable<OnboardingRoute> {
                OnboardingScreen(onFinished = {
                    navController.navigate(AuthRoute()) { popUpTo(OnboardingRoute) { inclusive = true } }
                })
            }

            composable<AuthRoute> { entry ->
                val route = entry.toRoute<AuthRoute>()
                val goHome = {
                    navController.navigate(HomeRoute) { popUpTo(navController.graph.id) { inclusive = true } }
                }
                AuthScreen(
                    showGuestOption = !route.returnAfterLogin,
                    onAuthenticated = { if (route.returnAfterLogin) navController.popBackStack() else goHome() },
                    onContinueAsGuest = goHome,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<HomeRoute> {
                HomeScreen(
                    onPlaceClick = { id -> navController.navigate(DetailsRoute(id)) },
                    onSearchClick = { category -> navController.navigate(SearchRoute(category?.key)) },
                )
            }
            composable<MapRoute> {
                MapScreen(onPlaceClick = { id -> navController.navigate(DetailsRoute(id)) })
            }
            composable<FavoritesRoute> {
                FavoritesScreen(
                    onPlaceClick = { id -> navController.navigate(DetailsRoute(id)) },
                    onExploreClick = { navController.navigateToTab(TopLevelTab.HOME) },
                )
            }
            composable<ProfileRoute> {
                ProfileScreen(onLoginClick = { navController.navigate(AuthRoute(returnAfterLogin = true)) })
            }
            composable<SearchRoute> {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onPlaceClick = { id -> navController.navigate(DetailsRoute(id)) },
                )
            }
            composable<DetailsRoute> {
                PlaceDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onWriteReview = { id -> navController.navigate(AddReviewRoute(id)) },
                    onLoginRequired = { navController.navigate(AuthRoute(returnAfterLogin = true)) },
                )
            }
            composable<AddReviewRoute> {
                AddReviewScreen(
                    onBack = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(AuthRoute(returnAfterLogin = true)) },
                )
            }
        }
    }
}

/** Standard bottom-nav behaviour: one copy of each tab, state restored when re-selected. */
private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
