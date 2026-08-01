package com.app.privacyscreendisplay.home.data.repository

import com.app.privacyscreendisplay.home.data.datasource.PrivacyGuardLocalDataSource
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.home.domain.model.ProtectionStatus
import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import com.app.privacyscreendisplay.home.domain.repository.PrivacyGuardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Repository implementation mapping Privacy Guard data operations to domain contract.
 */
class PrivacyGuardRepositoryImpl(
    private val localDataSource: PrivacyGuardLocalDataSource
) : PrivacyGuardRepository {

    override fun getProtectionStatus(): Flow<ProtectionStatus> {
        return localDataSource.protectionStatusFlow
    }

    override suspend fun setProtectionActive(active: Boolean) {
        localDataSource.setProtectionActive(active)
    }

    override suspend fun setOverlayStyle(style: OverlayStyle) {
        localDataSource.setOverlayStyle(style)
    }

    override suspend fun setSensitivityLevel(sensitivity: SensitivityLevel) {
        localDataSource.setSensitivityLevel(sensitivity)
    }
}
