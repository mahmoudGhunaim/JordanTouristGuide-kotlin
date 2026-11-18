package edu.zu.jordantouristguide

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import edu.zu.jordantouristguide.ui.navigation.AppNavHost
import edu.zu.jordantouristguide.ui.theme.JordanGuideTheme
import edu.zu.jordantouristguide.viewmodel.MainViewModel

/**
 * The only Activity. Everything else is Compose screens inside [AppNavHost].
 * It extends AppCompatActivity so the in-app language switch
 * (AppCompatDelegate.setApplicationLocales) also works on Android 8–12.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // status-bar icon colours are managed in ui/util/SystemBars.kt
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            JordanGuideTheme(themeMode = themeMode) {
                AppNavHost()
            }
        }
    }
}
