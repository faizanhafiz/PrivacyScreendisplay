package com.app.privacyscreendisplay.home.domain.usecase

import com.app.privacyscreendisplay.home.domain.model.ProtectionStatus
import com.app.privacyscreendisplay.home.domain.repository.PrivacyGuardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Single-responsibility use case interactor retrieving protection status state stream.
 */
class GetProtectionStatusUseCase(
    private val repository: PrivacyGuardRepository
) {
    operator fun invoke(): Flow<ProtectionStatus> {
        return repository.getProtectionStatus()
    }
}
