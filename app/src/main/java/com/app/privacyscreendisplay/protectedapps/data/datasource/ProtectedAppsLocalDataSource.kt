package com.app.privacyscreendisplay.protectedapps.data.datasource

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.protectedAppsDataStore: DataStore<Preferences> by preferencesDataStore(name = "protected_apps_preferences")

class ProtectedAppsLocalDataSource(
    private val context: Context
) {
    private val KEY_PROTECTED_PACKAGES = stringSetPreferencesKey("protected_package_names")

    // Default pre-filled items matching user screenshot: WhatsApp and PhonePe
    private val defaultApps = listOf(
        ProtectedApp(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            categoryName = "Messages",
            iconResName = "ic_whatsapp"
        ),
        ProtectedApp(
            packageName = "com.phonepe.app",
            appName = "PhonePe",
            categoryName = "Payments",
            iconResName = "ic_phonepe"
        )
    )

    fun getProtectedApps(): Flow<List<ProtectedApp>> {
        return context.protectedAppsDataStore.data.map { prefs ->
            val savedPackages = prefs[KEY_PROTECTED_PACKAGES]
            if (savedPackages == null) {
                defaultApps
            } else {
                val allKnown = defaultApps.associateBy { it.packageName }.toMutableMap()
                val pm = context.packageManager

                savedPackages.map { pkg ->
                    val defaultItem = allKnown[pkg]
                    if (defaultItem != null) {
                        defaultItem
                    } else {
                        val savedLabel = prefs[stringPreferencesKey("label_$pkg")]
                        val savedCategory = prefs[stringPreferencesKey("category_$pkg")]

                        val resolvedLabel = savedLabel ?: resolveSystemAppLabel(pm, pkg)
                        val resolvedCategory = savedCategory ?: resolveSystemAppCategory(pm, pkg)

                        ProtectedApp(
                            packageName = pkg,
                            appName = resolvedLabel,
                            categoryName = resolvedCategory,
                            iconResName = "ic_installed"
                        )
                    }
                }
            }
        }
    }

    suspend fun addProtectedApp(app: ProtectedApp) {
        context.protectedAppsDataStore.edit { prefs ->
            val current = prefs[KEY_PROTECTED_PACKAGES] ?: defaultApps.map { it.packageName }.toSet()
            prefs[KEY_PROTECTED_PACKAGES] = current + app.packageName
            prefs[stringPreferencesKey("label_${app.packageName}")] = app.appName
            prefs[stringPreferencesKey("category_${app.packageName}")] = app.categoryName
        }
    }

    suspend fun removeProtectedApp(packageName: String) {
        context.protectedAppsDataStore.edit { prefs ->
            val current = prefs[KEY_PROTECTED_PACKAGES] ?: defaultApps.map { it.packageName }.toSet()
            prefs[KEY_PROTECTED_PACKAGES] = current - packageName
            prefs.remove(stringPreferencesKey("label_$packageName"))
            prefs.remove(stringPreferencesKey("category_$packageName"))
        }
    }

    private fun resolveSystemAppLabel(pm: PackageManager, packageName: String): String {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                pm.getApplicationInfo(packageName, 0)
            }
            appInfo.loadLabel(pm).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.').replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    private fun resolveSystemAppCategory(pm: PackageManager, packageName: String): String {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                pm.getApplicationInfo(packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_GAME -> "Game"
                    ApplicationInfo.CATEGORY_AUDIO -> "Audio & Music"
                    ApplicationInfo.CATEGORY_VIDEO -> "Video"
                    ApplicationInfo.CATEGORY_IMAGE -> "Photos & Media"
                    ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                    ApplicationInfo.CATEGORY_NEWS -> "News"
                    ApplicationInfo.CATEGORY_MAPS -> "Maps & Navigation"
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                    ApplicationInfo.CATEGORY_ACCESSIBILITY -> "Utility"
                    else -> "Application"
                }
            } else {
                "Application"
            }
        } catch (e: Exception) {
            "Application"
        }
    }
}
