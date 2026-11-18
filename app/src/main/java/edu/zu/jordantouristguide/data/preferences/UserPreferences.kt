package edu.zu.jordantouristguide.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import edu.zu.jordantouristguide.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Small key-value settings stored with DataStore. */
interface UserPreferences {
    val onboardingCompleted: Flow<Boolean>
    val themeMode: Flow<ThemeMode>

    /** Signed-in user id for the offline demo auth (unused with Firebase). */
    val localSessionUserId: Flow<String?>

    suspend fun setOnboardingCompleted()
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLocalSessionUserId(userId: String?)
}

@Singleton
class DataStoreUserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferences {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SESSION_USER_ID = stringPreferencesKey("local_session_user_id")
    }

    override val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
            ?: ThemeMode.SYSTEM
    }

    override val localSessionUserId: Flow<String?> =
        dataStore.data.map { it[Keys.SESSION_USER_ID] }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setLocalSessionUserId(userId: String?) {
        dataStore.edit { prefs ->
            if (userId == null) prefs.remove(Keys.SESSION_USER_ID) else prefs[Keys.SESSION_USER_ID] = userId
        }
    }
}
