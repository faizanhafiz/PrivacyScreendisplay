package com.app.privacyscreendisplay.onboarding.domain.usecase

import com.app.privacyscreendisplay.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Single-responsibility use case interactor to verify if the user has already completed onboarding.
 *
 * @property repository Contract providing persistence operations for onboarding state.
 */
class IsOnboardingCompletedUseCase(
    private val repository: OnboardingRepository
) {
    /**
     * Executes the check for onboarding status.
     *
     * @return A [Flow] emitting `true` if completed, `false` otherwise.
     */
    operator fun invoke(): Flow<Boolean> {
        return repository.isOnboardingCompleted()
    }
}
