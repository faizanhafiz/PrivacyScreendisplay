package com.app.privacyscreendisplay.onboarding.di

import android.content.Context
import com.app.privacyscreendisplay.onboarding.data.datasource.OnboardingLocalDataSource
import com.app.privacyscreendisplay.onboarding.data.repository.OnboardingRepositoryImpl
import com.app.privacyscreendisplay.onboarding.domain.repository.OnboardingRepository
import com.app.privacyscreendisplay.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.app.privacyscreendisplay.onboarding.domain.usecase.GetOnboardingFeaturesUseCase
import com.app.privacyscreendisplay.onboarding.domain.usecase.IsOnboardingCompletedUseCase

/**
 * Dependency Injection factory provider for Onboarding feature components.
 * Adheres to Dependency Inversion Principle (DIP) and modular setup.
 */
object OnboardingModule {

    /**
     * Provides the local DataStore data source.
     */
    fun provideOnboardingLocalDataSource(context: Context): OnboardingLocalDataSource {
        return OnboardingLocalDataSource(context)
    }

    /**
     * Provides concrete repository implementation bound to domain interface.
     */
    fun provideOnboardingRepository(localDataSource: OnboardingLocalDataSource): OnboardingRepository {
        return OnboardingRepositoryImpl(localDataSource)
    }

    /**
     * Provides IsOnboardingCompletedUseCase interactor.
     */
    fun provideIsOnboardingCompletedUseCase(repository: OnboardingRepository): IsOnboardingCompletedUseCase {
        return IsOnboardingCompletedUseCase(repository)
    }

    /**
     * Provides CompleteOnboardingUseCase interactor.
     */
    fun provideCompleteOnboardingUseCase(repository: OnboardingRepository): CompleteOnboardingUseCase {
        return CompleteOnboardingUseCase(repository)
    }

    /**
     * Provides GetOnboardingFeaturesUseCase interactor.
     */
    fun provideGetOnboardingFeaturesUseCase(repository: OnboardingRepository): GetOnboardingFeaturesUseCase {
        return GetOnboardingFeaturesUseCase(repository)
    }
}
