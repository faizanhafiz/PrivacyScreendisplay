package com.app.privacyscreendisplay.protectedapps.data.repository

import com.app.privacyscreendisplay.protectedapps.data.datasource.ProtectedAppsLocalDataSource
import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp
import com.app.privacyscreendisplay.protectedapps.domain.repository.ProtectedAppsRepository
import kotlinx.coroutines.flow.Flow

class ProtectedAppsRepositoryImpl(
    private val localDataSource: ProtectedAppsLocalDataSource
) : ProtectedAppsRepository {

    override fun getProtectedApps(): Flow<List<ProtectedApp>> {
        return localDataSource.getProtectedApps()
    }

    override fun getMaxFreeAllowedApps(): Int = 2

    override suspend fun addProtectedApp(app: ProtectedApp) {
        localDataSource.addProtectedApp(app)
    }

    override suspend fun removeProtectedApp(packageName: String) {
        localDataSource.removeProtectedApp(packageName)
    }
}
