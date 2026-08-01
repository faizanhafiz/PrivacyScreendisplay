package com.app.privacyscreendisplay.onboarding.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "privacy_guard_preferences")

/**
 * Local Data Source responsible for managing persistent onboarding status via Jetpack DataStore Preferences.
 *
 * @property context Application context used to access DataStore.
 */
class OnboardingLocalDataSource(
    private val context: Context
) {

    private object PreferencesKeys {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("key_onboarding_completed")
    }

    /**
     * Observes the onboarding completion state from local storage.
     *
     * @return Flow emitting boolean indicating whether onboarding has been completed.
     */
    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_ONBOARDING_COMPLETED] ?: false
        }

    /**
     * Updates the onboarding completion flag in DataStore Preferences.
     *
     * @param completed `true` if completed, `false` otherwise.
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_ONBOARDING_COMPLETED] = completed
        }
    }
}
