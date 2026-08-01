package com.app.privacyscreendisplay.onboarding.domain.repository

import com.app.privacyscreendisplay.onboarding.domain.model.OnboardingFeatureItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract defining storage and retrieval operations for the onboarding flow.
 * Adheres to Dependency Inversion Principle (DIP).
 */
interface OnboardingRepository {

    /**
     * Checks if the user has completed the onboarding flow.
     *
     * @return A [Flow] emitting `true` if onboarding is complete, `false` otherwise.
     */
    fun isOnboardingCompleted(): Flow<Boolean>

    /**
     * Persists the onboarding completion status.
     *
     * @param completed State flag indicating whether onboarding has been completed.
     */
    suspend fun setOnboardingCompleted(completed: Boolean)

    /**
     * Fetches the feature highlight items to display during onboarding.
     *
     * @return List of [OnboardingFeatureItem] containing feature descriptions and icons.
     */
    fun getOnboardingFeatures(): List<OnboardingFeatureItem>
}
