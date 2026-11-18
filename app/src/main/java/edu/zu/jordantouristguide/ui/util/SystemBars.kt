package edu.zu.jordantouristguide.ui.util

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Keeps the status/navigation bar icons readable (the app draws edge-to-edge).
 * Icons are white in dark theme, and also when [lightIcons] is true because the
 * screen shows a photo or the rose-red header behind the status bar.
 */
@Composable
fun SystemBarIconsEffect(lightIcons: Boolean) {
    val view = LocalView.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDarkTheme && !lightIcons
            isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}
