package com.app.privacyscreendisplay.protectedapps.domain.usecase

import com.app.privacyscreendisplay.protectedapps.domain.repository.ProtectedAppsRepository

class RemoveProtectedAppUseCase(
    private val repository: ProtectedAppsRepository
) {
    suspend operator fun invoke(packageName: String) {
        repository.removeProtectedApp(packageName)
    }
}
