package com.app.privacyscreendisplay.home.presentation.viewmodel

import com.app.privacyscreendisplay.home.domain.model.ProtectionStatus

/**
 * Immutable UI State representing the Home screen state model.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val protectionStatus: ProtectionStatus = ProtectionStatus()
)
