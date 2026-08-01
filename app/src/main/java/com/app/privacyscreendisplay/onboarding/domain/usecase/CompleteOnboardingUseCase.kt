package com.app.privacyscreendisplay.onboarding.domain.usecase

import com.app.privacyscreendisplay.onboarding.domain.repository.OnboardingRepository

/**
 * Single-responsibility use case interactor responsible for marking the onboarding flow as completed.
 *
 * @property repository Contract providing persistence operations for onboarding state.
 */
class CompleteOnboardingUseCase(
    private val repository: OnboardingRepository
) {
    /**
     * Executes marking onboarding as completed in local storage.
     */
    suspend operator fun invoke() {
        repository.setOnboardingCompleted(true)
    }
}
