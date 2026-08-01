package com.app.privacyscreendisplay.onboarding.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.privacyscreendisplay.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.app.privacyscreendisplay.onboarding.domain.usecase.GetOnboardingFeaturesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating state management and user actions for the Onboarding Screen.
 * Exposes immutable state streams using Kotlin Coroutines and StateFlow/SharedFlow.
 *
 * @property getOnboardingFeaturesUseCase Use case to load features.
 * @property completeOnboardingUseCase Use case to persist completed status.
 */
class OnboardingViewModel(
    private val getOnboardingFeaturesUseCase: GetOnboardingFeaturesUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<OnboardingUiEvent>()
    val uiEvent: SharedFlow<OnboardingUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadOnboardingContent()
    }

    private fun loadOnboardingContent() {
        viewModelScope.launch {
            val featureList = getOnboardingFeaturesUseCase()
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    features = featureList
                )
            }
        }
    }

    /**
     * Handles user tap on "Get Started" primary action button.
     * Persists onboarding completion and dispatches navigation event.
     */
    fun onGetStartedClicked() {
        viewModelScope.launch {
            completeOnboardingUseCase()
            _uiEvent.emit(OnboardingUiEvent.NavigateToHome)
        }
    }

    /**
     * Handles user tap on "Learn more" secondary text button.
     */
    fun onLearnMoreClicked() {
        viewModelScope.launch {
            _uiEvent.emit(OnboardingUiEvent.NavigateToLearnMore)
        }
    }
}
