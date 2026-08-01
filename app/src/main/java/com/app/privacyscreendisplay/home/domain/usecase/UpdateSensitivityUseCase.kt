package com.app.privacyscreendisplay.home.domain.usecase

import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import com.app.privacyscreendisplay.home.domain.repository.PrivacyGuardRepository

/**
 * Single-responsibility use case interactor updating the AI detection sensitivity level.
 */
class UpdateSensitivityUseCase(
    private val repository: PrivacyGuardRepository
) {
    suspend operator fun invoke(sensitivity: SensitivityLevel) {
        repository.setSensitivityLevel(sensitivity)
    }
}
