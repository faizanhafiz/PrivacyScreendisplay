package com.app.privacyscreendisplay.onboarding.presentation.viewmodel

/**
 * Sealed interface representing one-shot UI side-effects dispatched from OnboardingViewModel to UI layer.
 */
sealed interface OnboardingUiEvent {
    /**
     * Triggered when the user successfully clicks "Get Started" and onboarding completes.
     */
    data object NavigateToHome : OnboardingUiEvent

    /**
     * Triggered when the user clicks "Learn more" link.
     */
    data object NavigateToLearnMore : OnboardingUiEvent
}
