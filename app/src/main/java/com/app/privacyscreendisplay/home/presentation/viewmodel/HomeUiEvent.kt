package com.app.privacyscreendisplay.home.presentation.viewmodel

/**
 * Sealed interface representing one-off side-effects dispatched from HomeViewModel.
 */
sealed interface HomeUiEvent {
    data object NavigateToProtectedApps : HomeUiEvent
    data object NavigateToActivityLog : HomeUiEvent
    data object NavigateToPremiumPurchase : HomeUiEvent
    data object NavigateToSettings : HomeUiEvent
    data class ShowToast(val message: String) : HomeUiEvent
}
