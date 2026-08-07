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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.util.Locale

private val Context.protectedAppsDataStore: DataStore<Preferences> by preferencesDataStore(name = "protected_apps_preferences")

private val tickerFlow: Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(2000L)
    }
}

/**
 * Local Data Source managing persistent protected applications via Jetpack DataStore.
 * Ensures initial installs start with an empty list and automatically prunes uninstalled apps.
 */
class ProtectedAppsLocalDataSource(
    private val context: Context
) {
    private val KEY_PROTECTED_PACKAGES = stringSetPreferencesKey("protected_package_names")
    private val KEY_PROTECTED_PACKAGES_ORDER = stringPreferencesKey("protected_packages_order")

    /**
     * Observes protected apps from DataStore in exact insertion order.
     * Automatically prunes excess apps down to free limit (2 apps) when premium expires.
     */
    fun getProtectedApps(): Flow<List<ProtectedApp>> {
        return combine(
            context.protectedAppsDataStore.data,
            tickerFlow
        ) { prefs, _ ->
            val savedSet = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
            val orderString = prefs[KEY_PROTECTED_PACKAGES_ORDER] ?: ""

            val orderedList = if (orderString.isNotBlank()) {
                val listFromOrder = orderString.split(",").filter { it.isNotBlank() && savedSet.contains(it) }
                // Include any package in set that wasn't in order string
                val missingFromOrder = savedSet.filter { !listFromOrder.contains(it) }
                listFromOrder + missingFromOrder
            } else {
                savedSet.toList()
            }

            if (orderedList.isEmpty()) {
                emptyList()
            } else {
                val pm = context.packageManager
                val validApps = mutableListOf<ProtectedApp>()
                val uninstalledPackages = mutableSetOf<String>()

                for (pkg in orderedList) {
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

                // If premium is NOT active and apps count exceeds free limit (2), trim excess apps
                val isPremiumActive = com.app.privacyscreendisplay.core.ads.config.AdConfig.isPremiumUser
                val maxFreeLimit = 2

                if (!isPremiumActive && validApps.size > maxFreeLimit) {
                    val allowedApps = validApps.take(maxFreeLimit)
                    val excessApps = validApps.drop(maxFreeLimit).map { it.packageName }
                    pruneUninstalledPackages(excessApps.toSet())
                    allowedApps
                } else {
                    validApps
                }
            }
        }.distinctUntilChanged()
    }

    suspend fun addProtectedApp(app: ProtectedApp) {
        context.protectedAppsDataStore.edit { prefs ->
            val currentSet = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
            val orderString = prefs[KEY_PROTECTED_PACKAGES_ORDER] ?: ""
            val currentOrder = if (orderString.isNotBlank()) orderString.split(",").filter { it.isNotBlank() } else emptyList()

            val newSet = currentSet + app.packageName
            val newOrder = if (!currentOrder.contains(app.packageName)) currentOrder + app.packageName else currentOrder

            prefs[KEY_PROTECTED_PACKAGES] = newSet
            prefs[KEY_PROTECTED_PACKAGES_ORDER] = newOrder.joinToString(",")
            prefs[stringPreferencesKey("label_${app.packageName}")] = app.appName
            prefs[stringPreferencesKey("category_${app.packageName}")] = app.categoryName
        }
    }

    suspend fun removeProtectedApp(packageName: String) {
        context.protectedAppsDataStore.edit { prefs ->
            val currentSet = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
            val orderString = prefs[KEY_PROTECTED_PACKAGES_ORDER] ?: ""
            val currentOrder = if (orderString.isNotBlank()) orderString.split(",").filter { it.isNotBlank() } else emptyList()

            val newSet = currentSet - packageName
            val newOrder = currentOrder - packageName

            prefs[KEY_PROTECTED_PACKAGES] = newSet
            prefs[KEY_PROTECTED_PACKAGES_ORDER] = newOrder.joinToString(",")
            prefs.remove(stringPreferencesKey("label_$packageName"))
            prefs.remove(stringPreferencesKey("category_$packageName"))
        }
    }

    private suspend fun pruneUninstalledPackages(uninstalled: Set<String>) {
        try {
            context.protectedAppsDataStore.edit { prefs ->
                val currentSet = prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
                val orderString = prefs[KEY_PROTECTED_PACKAGES_ORDER] ?: ""
                val currentOrder = if (orderString.isNotBlank()) orderString.split(",").filter { it.isNotBlank() } else emptyList()

                val newSet = currentSet - uninstalled
                val newOrder = currentOrder - uninstalled

                prefs[KEY_PROTECTED_PACKAGES] = newSet
                prefs[KEY_PROTECTED_PACKAGES_ORDER] = newOrder.joinToString(",")
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
