package com.app.privacyscreendisplay.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import com.app.privacyscreendisplay.home.domain.usecase.GetProtectionStatusUseCase
import com.app.privacyscreendisplay.home.domain.usecase.ToggleProtectionUseCase
import com.app.privacyscreendisplay.home.domain.usecase.UpdateOverlayStyleUseCase
import com.app.privacyscreendisplay.home.domain.usecase.UpdateSensitivityUseCase
import com.app.privacyscreendisplay.protectedapps.domain.usecase.GetProtectedAppsUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating state management and user actions for the Home Screen.
 */
class HomeViewModel(
    private val getProtectionStatusUseCase: GetProtectionStatusUseCase,
    private val getProtectedAppsUseCase: GetProtectedAppsUseCase,
    private val toggleProtectionUseCase: ToggleProtectionUseCase,
    private val updateOverlayStyleUseCase: UpdateOverlayStyleUseCase,
    private val updateSensitivityUseCase: UpdateSensitivityUseCase
) : ViewModel() {

    private var hasShownLaunchAd = false

    fun shouldShowLaunchAd(): Boolean {
        if (!hasShownLaunchAd) {
            hasShownLaunchAd = true
            return true
        }
        return false
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEvent: SharedFlow<HomeUiEvent> = _uiEvent.asSharedFlow()

    init {
        observeProtectionStatus()
    }

    private fun observeProtectionStatus() {
        viewModelScope.launch {
            combine(
                getProtectionStatusUseCase(),
                getProtectedAppsUseCase()
            ) { status, apps ->
                status.copy(protectedAppsCount = apps.size)
            }.collectLatest { status ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        protectionStatus = status
                    )
                }
            }
        }
    }

    fun onToggleProtectionClicked() {
        viewModelScope.launch {
            val currentState = _uiState.value.protectionStatus.isProtectionActive
            toggleProtectionUseCase(currentState)
            val msg = if (!currentState) "Privacy Guard Activated" else "Privacy Guard Deactivated"
            _uiEvent.emit(HomeUiEvent.ShowToast(msg))
        }
    }

    fun enableProtection() {
        viewModelScope.launch {
            if (!_uiState.value.protectionStatus.isProtectionActive) {
                toggleProtectionUseCase(false)
                _uiEvent.emit(HomeUiEvent.ShowToast("Privacy Guard Activated"))
            }
        }
    }

    fun onOverlayStyleSelected(style: OverlayStyle) {
        viewModelScope.launch {
            if (style.isPremium && !_uiState.value.protectionStatus.isPremiumSubscriber) {
                _uiEvent.emit(HomeUiEvent.NavigateToPremiumPurchase)
            } else {
                updateOverlayStyleUseCase(style)
                _uiEvent.emit(HomeUiEvent.ShowToast("Overlay style updated to ${style.displayName}"))
            }
        }
    }

    fun onSensitivitySelected(sensitivity: SensitivityLevel) {
        viewModelScope.launch {
            updateSensitivityUseCase(sensitivity)
            _uiEvent.emit(HomeUiEvent.ShowToast("Sensitivity set to ${sensitivity.displayName}"))
        }
    }

    fun onProtectedAppsClicked() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavigateToProtectedApps)
        }
    }

    fun onActivityLogClicked() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavigateToActivityLog)
        }
    }

    fun onPremiumBannerClicked() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavigateToPremiumPurchase)
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavigateToSettings)
        }
    }
}
