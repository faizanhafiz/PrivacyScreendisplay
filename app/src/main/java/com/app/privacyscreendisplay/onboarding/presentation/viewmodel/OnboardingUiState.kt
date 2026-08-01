package com.app.privacyscreendisplay.onboarding.presentation.viewmodel

import com.app.privacyscreendisplay.onboarding.domain.model.OnboardingFeatureItem

/**
 * Immutable UI State representing the Onboarding screen rendering model.
 *
 * @property isLoading Indicates if state initial loading is in progress.
 * @property features List of feature highlight items to display.
 */
data class OnboardingUiState(
    val isLoading: Boolean = false,
    val features: List<OnboardingFeatureItem> = emptyList()
)
