package com.app.privacyscreendisplay.protectedapps.domain.usecase

import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp
import com.app.privacyscreendisplay.protectedapps.domain.repository.ProtectedAppsRepository

class AddProtectedAppUseCase(
    private val repository: ProtectedAppsRepository
) {
    suspend operator fun invoke(app: ProtectedApp) {
        repository.addProtectedApp(app)
    }
}
