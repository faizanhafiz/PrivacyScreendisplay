package com.app.privacyscreendisplay.protectedapps.presentation.viewmodel

import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp

data class ProtectedAppsUiState(
    val protectedApps: List<ProtectedApp> = emptyList(),
    val maxFreeAppsAllowed: Int = 2,
    val isPremiumUser: Boolean = false,
    val isLoading: Boolean = false
) {
    val currentAppsCount: Int get() = protectedApps.size
    val isQuotaReached: Boolean get() = !isPremiumUser && currentAppsCount >= maxFreeAppsAllowed
}
