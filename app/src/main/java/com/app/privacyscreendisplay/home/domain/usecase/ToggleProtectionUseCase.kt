package com.app.privacyscreendisplay.home.domain.usecase

import com.app.privacyscreendisplay.home.domain.repository.PrivacyGuardRepository

/**
 * Single-responsibility use case interactor toggling active state of the privacy guard service.
 */
class ToggleProtectionUseCase(
    private val repository: PrivacyGuardRepository
) {
    suspend operator fun invoke(currentlyActive: Boolean) {
        repository.setProtectionActive(!currentlyActive)
    }
}
