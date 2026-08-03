package com.app.privacyscreendisplay.home.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.privacyscreendisplay.core.ads.AdConfig
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.home.domain.model.ProtectionStatus
import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.io.IOException

private val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = "privacy_guard_home_preferences")

private val tickerFlow: Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(2000L)
    }
}

/**
 * Local Data Source for managing persistent Home Screen protection settings via Jetpack DataStore.
 */
class PrivacyGuardLocalDataSource(
    private val context: Context
) {

    private object Keys {
        val PROTECTION_ACTIVE = booleanPreferencesKey("key_protection_active")
        val OVERLAY_STYLE = stringPreferencesKey("key_overlay_style")
        val SENSITIVITY_LEVEL = stringPreferencesKey("key_sensitivity_level")
        val IS_PREMIUM = booleanPreferencesKey("key_is_premium")
        val PREMIUM_EXPIRATION = longPreferencesKey("key_premium_expiration")
    }

    val protectionStatusFlow: Flow<ProtectionStatus> = combine(
        context.homeDataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        },
        tickerFlow
    ) { preferences, _ ->
        val isActivePref = preferences[Keys.PROTECTION_ACTIVE] ?: true
        val styleName = preferences[Keys.OVERLAY_STYLE] ?: OverlayStyle.BLUR.name
        val sensitivityName = preferences[Keys.SENSITIVITY_LEVEL] ?: SensitivityLevel.MEDIUM.name
        val isPremiumPersisted = preferences[Keys.IS_PREMIUM] ?: false
        val expirationTimestamp = preferences[Keys.PREMIUM_EXPIRATION] ?: 0L
        val is24hActive = System.currentTimeMillis() < expirationTimestamp

        // Verify that all 3 required permissions are granted before allowing active status
        val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasOverlay = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(context)
        val hasUsageAccess = com.app.privacyscreendisplay.core.monitor.ForegroundAppMonitor(context).hasUsageAccessPermission()
        val allPermissionsGranted = hasCamera && hasOverlay && hasUsageAccess

        val isProtectionActiveEffective = isActivePref && allPermissionsGranted

        // Effective premium depends ONLY on permanent purchase OR active non-expired timer
        val isPremiumEffective = isPremiumPersisted || is24hActive

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

        // Sync global AdConfig state with effective subscription entitlement
        AdConfig.isPremiumUser = isPremiumEffective

        ProtectionStatus(
            isProtectionActive = isProtectionActiveEffective,
            selectedOverlayStyle = overlayStyle,
            sensitivity = sensitivity,
            protectedAppsCount = 0,
            detectionsToday = 4,
            isPremiumSubscriber = isPremiumEffective
        )
    }.distinctUntilChanged()

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

    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.homeDataStore.edit { preferences ->
            preferences[Keys.IS_PREMIUM] = isPremium
        }
        AdConfig.isPremiumUser = isPremium
    }

    suspend fun grant24HourPremium() {
        // Testing mode: 2 minutes duration (2 * 60 * 1000L)
        val expirationTime = System.currentTimeMillis() + (2 * 60 * 1000L)
        context.homeDataStore.edit { preferences ->
            preferences[Keys.PREMIUM_EXPIRATION] = expirationTime
        }
    }
}
