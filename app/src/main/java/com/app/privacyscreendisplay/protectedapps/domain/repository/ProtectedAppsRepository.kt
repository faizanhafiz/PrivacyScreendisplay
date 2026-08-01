package com.app.privacyscreendisplay.protectedapps.domain.repository

import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract managing protected applications state.
 */
interface ProtectedAppsRepository {
    fun getProtectedApps(): Flow<List<ProtectedApp>>
    fun getMaxFreeAllowedApps(): Int
    suspend fun addProtectedApp(app: ProtectedApp)
    suspend fun removeProtectedApp(packageName: String)
}
