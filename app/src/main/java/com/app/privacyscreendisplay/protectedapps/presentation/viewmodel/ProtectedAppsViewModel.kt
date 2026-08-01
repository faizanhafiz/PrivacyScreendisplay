package com.app.privacyscreendisplay.protectedapps.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp
import com.app.privacyscreendisplay.protectedapps.domain.usecase.AddProtectedAppUseCase
import com.app.privacyscreendisplay.protectedapps.domain.usecase.GetProtectedAppsUseCase
import com.app.privacyscreendisplay.protectedapps.domain.usecase.RemoveProtectedAppUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtectedAppsViewModel(
    private val getProtectedAppsUseCase: GetProtectedAppsUseCase,
    private val addProtectedAppUseCase: AddProtectedAppUseCase,
    private val removeProtectedAppUseCase: RemoveProtectedAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtectedAppsUiState(maxFreeAppsAllowed = getProtectedAppsUseCase.getMaxAllowed()))
    val uiState: StateFlow<ProtectedAppsUiState> = _uiState.asStateFlow()

    init {
        loadProtectedApps()
    }

    private fun loadProtectedApps() {
        viewModelScope.launch {
            getProtectedAppsUseCase().collect { apps ->
                _uiState.update { it.copy(protectedApps = apps, isLoading = false) }
            }
        }
    }

    fun onAddAppClicked(app: ProtectedApp) {
        viewModelScope.launch {
            addProtectedAppUseCase(app)
        }
    }

    fun onRemoveAppClicked(packageName: String) {
        viewModelScope.launch {
            removeProtectedAppUseCase(packageName)
        }
    }
}
