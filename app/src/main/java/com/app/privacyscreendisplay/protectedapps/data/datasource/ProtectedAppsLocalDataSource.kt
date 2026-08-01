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

/**
 * Local Data Source managing persistent protected applications via Jetpack DataStore.
 * Ensures initial installs start with an empty list and automatically prunes uninstalled apps.
 */
class ProtectedAppsLocalDataSource(
    private val context: Context
) {
    private val KEY_PROTECTED_PACKAGES = stringSetPreferencesKey("protected_package_names")

    /**
     * Observes protected apps from DataStore.
     * Automatically filters out and cleans up any packages that were uninstalled from the device.
     */
    fun getProtectedApps(): Flow<List<ProtectedApp>> {
        return context.protectedAppsDataStore.data.map { prefs ->
            val savedPackages = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
            if (savedPackages.isEmpty()) {
                emptyList()
            } else {
                val pm = context.packageManager
                val validApps = mutableListOf<ProtectedApp>()
                val uninstalledPackages = mutableSetOf<String>()

                for (pkg in savedPackages) {
                    if (isPackageInstalled(pm, pkg)) {
                        val savedLabel = prefs[stringPreferencesKey("label_$pkg")]
                        val savedCategory = prefs[stringPreferencesKey("category_$pkg")]

                        val resolvedLabel = savedLabel ?: resolveSystemAppLabel(pm, pkg)
                        val resolvedCategory = savedCategory ?: resolveSystemAppCategory(pm, pkg)

                        validApps.add(
                            ProtectedApp(
                                packageName = pkg,
                                appName = resolvedLabel,
                                categoryName = resolvedCategory,
                                iconResName = "ic_installed"
                            )
                        )
                    } else {
                        uninstalledPackages.add(pkg)
                    }
                }

                // Asynchronously prune uninstalled packages from DataStore if any detected
                if (uninstalledPackages.isNotEmpty()) {
                    pruneUninstalledPackages(uninstalledPackages)
                }

                validApps
            }
        }
    }

    suspend fun addProtectedApp(app: ProtectedApp) {
        context.protectedAppsDataStore.edit { prefs ->
            val current = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
            prefs[KEY_PROTECTED_PACKAGES] = current + app.packageName
            prefs[stringPreferencesKey("label_${app.packageName}")] = app.appName
            prefs[stringPreferencesKey("category_${app.packageName}")] = app.categoryName
        }
    }

    suspend fun removeProtectedApp(packageName: String) {
        context.protectedAppsDataStore.edit { prefs ->
            val current = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
            prefs[KEY_PROTECTED_PACKAGES] = current - packageName
            prefs.remove(stringPreferencesKey("label_$packageName"))
            prefs.remove(stringPreferencesKey("category_$packageName"))
        }
    }

    private suspend fun pruneUninstalledPackages(uninstalled: Set<String>) {
        try {
            context.protectedAppsDataStore.edit { prefs ->
                val current = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
                prefs[KEY_PROTECTED_PACKAGES] = current - uninstalled
                for (pkg in uninstalled) {
                    prefs.remove(stringPreferencesKey("label_$pkg"))
                    prefs.remove(stringPreferencesKey("category_$pkg"))
                }
            }
        } catch (_: Exception) {
            // Ignore concurrent edit exceptions
        }
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                pm.getApplicationInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
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
