package com.app.privacyscreendisplay.home.domain.usecase

import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.home.domain.repository.PrivacyGuardRepository

/**
 * Single-responsibility use case interactor updating the privacy screen overlay style.
 */
class UpdateOverlayStyleUseCase(
    private val repository: PrivacyGuardRepository
) {
    suspend operator fun invoke(style: OverlayStyle) {
        repository.setOverlayStyle(style)
    }
}
