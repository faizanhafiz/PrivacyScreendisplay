package com.app.privacyscreendisplay.home.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.home.domain.model.ProtectionStatus
import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = "privacy_guard_home_settings")

/**
 * Data Source managing real-time protection preferences and overlay configurations.
 */
class PrivacyGuardLocalDataSource(
    private val context: Context
) {
    private object Keys {
        val PROTECTION_ACTIVE = booleanPreferencesKey("key_protection_active")
        val OVERLAY_STYLE = stringPreferencesKey("key_overlay_style")
        val SENSITIVITY_LEVEL = stringPreferencesKey("key_sensitivity_level")
    }

    val protectionStatusFlow: Flow<ProtectionStatus> = context.homeDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val isActive = preferences[Keys.PROTECTION_ACTIVE] ?: true
            val styleName = preferences[Keys.OVERLAY_STYLE] ?: OverlayStyle.BLUR.name
            val sensitivityName = preferences[Keys.SENSITIVITY_LEVEL] ?: SensitivityLevel.MEDIUM.name

            val overlayStyle = try {
                OverlayStyle.valueOf(styleName)
            } catch (_: Exception) {
                OverlayStyle.BLUR
            }

            val sensitivity = try {
                SensitivityLevel.valueOf(sensitivityName)
            } catch (_: Exception) {
                SensitivityLevel.MEDIUM
            }

            ProtectionStatus(
                isProtectionActive = isActive,
                selectedOverlayStyle = overlayStyle,
                sensitivity = sensitivity,
                protectedAppsCount = 8,
                detectionsToday = 4,
                isPremiumSubscriber = false
            )
        }

    suspend fun setProtectionActive(active: Boolean) {
        context.homeDataStore.edit { preferences ->
            preferences[Keys.PROTECTION_ACTIVE] = active
        }
    }

    suspend fun setOverlayStyle(style: OverlayStyle) {
        context.homeDataStore.edit { preferences ->
            preferences[Keys.OVERLAY_STYLE] = style.name
        }
    }

    suspend fun setSensitivityLevel(sensitivity: SensitivityLevel) {
        context.homeDataStore.edit { preferences ->
            preferences[Keys.SENSITIVITY_LEVEL] = sensitivity.name
        }
    }
}
