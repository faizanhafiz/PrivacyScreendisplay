package com.app.privacyscreendisplay.protectedapps.domain.usecase

import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp
import com.app.privacyscreendisplay.protectedapps.domain.repository.ProtectedAppsRepository
import kotlinx.coroutines.flow.Flow

class GetProtectedAppsUseCase(
    private val repository: ProtectedAppsRepository
) {
    operator fun invoke(): Flow<List<ProtectedApp>> {
        return repository.getProtectedApps()
    }

    fun getMaxAllowed(): Int = repository.getMaxFreeAllowedApps()
}
