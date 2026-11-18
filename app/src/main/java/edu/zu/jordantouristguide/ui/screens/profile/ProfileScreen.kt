package edu.zu.jordantouristguide.ui.screens.profile

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.zu.jordantouristguide.BuildConfig
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.config.AppConfig
import edu.zu.jordantouristguide.data.model.ThemeMode
import edu.zu.jordantouristguide.data.model.User
import edu.zu.jordantouristguide.ui.components.LoadingState
import edu.zu.jordantouristguide.ui.util.isArabic
import edu.zu.jordantouristguide.viewmodel.ProfileViewModel

private val TEAM = listOf(
    "Mahmoud Ghunaim (202300089)",
    "Ahmed Abu Rabie (202303206)",
    "Khalid Jawlaj (202301219)",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.profile_title)) }) }) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UserCard(user = state.user, onLoginClick = onLoginClick)

            SettingsCard(title = stringResource(R.string.profile_settings)) {
                SettingLabel(Icons.Outlined.Language, stringResource(R.string.profile_language))
                LanguageSelector()
                Spacer(Modifier.height(8.dp))
                SettingLabel(Icons.Outlined.DarkMode, stringResource(R.string.profile_theme))
                ThemeSelector(selected = state.themeMode, onSelect = viewModel::setThemeMode)
            }

            SettingsCard(title = stringResource(R.string.profile_about)) {
                SettingLabel(
                    Icons.Outlined.CloudQueue,
                    stringResource(R.string.profile_data_source) + ": " + stringResource(
                        if (AppConfig.isFirebaseEnabled) R.string.profile_data_firebase else R.string.profile_data_local,
                    ),
                )
                SettingLabel(Icons.Outlined.Info, stringResource(R.string.profile_about_text))
                TEAM.forEach { member ->
                    Text("•  $member", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 32.dp))
                }
                Text(
                    stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                )
            }

            if (state.user != null) {
                OutlinedButton(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_logout))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun UserCard(user: User?, onLoginClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (user != null) {
                    Text(
                        user.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    user?.name ?: stringResource(R.string.profile_guest),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    user?.email ?: stringResource(R.string.profile_guest_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                if (user == null) {
                    Button(onClick = onLoginClick, modifier = Modifier.padding(top = 10.dp)) {
                        Text(stringResource(R.string.profile_login))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun SettingLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Switches the app language with AppCompat's per-app locale API.
 * The activity is recreated automatically and Compose re-reads strings.xml / values-ar,
 * and the layout flips to right-to-left for Arabic.
 */
@Composable
private fun LanguageSelector() {
    val arabic = isArabic()
    val options = listOf("en" to R.string.profile_language_en, "ar" to R.string.profile_language_ar)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (tag, label) ->
            SegmentedButton(
                selected = (tag == "ar") == arabic,
                onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label)) }
        }
    }
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to R.string.profile_theme_system,
        ThemeMode.LIGHT to R.string.profile_theme_light,
        ThemeMode.DARK to R.string.profile_theme_dark,
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label)) }
        }
    }
}
