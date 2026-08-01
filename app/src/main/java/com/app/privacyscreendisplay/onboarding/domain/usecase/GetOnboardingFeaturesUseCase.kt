package com.app.privacyscreendisplay.onboarding.domain.usecase

import com.app.privacyscreendisplay.onboarding.domain.model.OnboardingFeatureItem
import com.app.privacyscreendisplay.onboarding.domain.repository.OnboardingRepository

/**
 * Single-responsibility use case interactor supplying the ordered list of onboarding feature highlights.
 *
 * @property repository Domain repository providing access to onboarding metadata.
 */
class GetOnboardingFeaturesUseCase(
    private val repository: OnboardingRepository
) {
    /**
     * Retrieves the curated feature list for presentation.
     *
     * @return List of [OnboardingFeatureItem] matching design requirements.
     */
    operator fun invoke(): List<OnboardingFeatureItem> {
        return repository.getOnboardingFeatures()
    }
}
