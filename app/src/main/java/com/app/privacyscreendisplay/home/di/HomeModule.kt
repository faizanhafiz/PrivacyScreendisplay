package com.app.privacyscreendisplay.home.di

import android.content.Context
import com.app.privacyscreendisplay.home.data.datasource.PrivacyGuardLocalDataSource
import com.app.privacyscreendisplay.home.data.repository.PrivacyGuardRepositoryImpl
import com.app.privacyscreendisplay.home.domain.repository.PrivacyGuardRepository
import com.app.privacyscreendisplay.home.domain.usecase.GetProtectionStatusUseCase
import com.app.privacyscreendisplay.home.domain.usecase.ToggleProtectionUseCase
import com.app.privacyscreendisplay.home.domain.usecase.UpdateOverlayStyleUseCase
import com.app.privacyscreendisplay.home.domain.usecase.UpdateSensitivityUseCase

/**
 * Dependency Injection factory provider for the Home Feature.
 */
object HomeModule {

    fun providePrivacyGuardLocalDataSource(context: Context): PrivacyGuardLocalDataSource {
        return PrivacyGuardLocalDataSource(context)
    }

    fun providePrivacyGuardRepository(localDataSource: PrivacyGuardLocalDataSource): PrivacyGuardRepository {
        return PrivacyGuardRepositoryImpl(localDataSource)
    }

    fun provideGetProtectionStatusUseCase(repository: PrivacyGuardRepository): GetProtectionStatusUseCase {
        return GetProtectionStatusUseCase(repository)
    }

    fun provideToggleProtectionUseCase(repository: PrivacyGuardRepository): ToggleProtectionUseCase {
        return ToggleProtectionUseCase(repository)
    }

    fun provideUpdateOverlayStyleUseCase(repository: PrivacyGuardRepository): UpdateOverlayStyleUseCase {
        return UpdateOverlayStyleUseCase(repository)
    }

    fun provideUpdateSensitivityUseCase(repository: PrivacyGuardRepository): UpdateSensitivityUseCase {
        return UpdateSensitivityUseCase(repository)
    }
}
