package com.app.privacyscreendisplay.protectedapps.presentation.viewmodel

import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp

sealed interface ProtectedAppsUiEvent {
    data class AddApp(val app: ProtectedApp) : ProtectedAppsUiEvent
    data class RemoveApp(val packageName: String) : ProtectedAppsUiEvent
    object UpgradeToPremiumClicked : ProtectedAppsUiEvent
}
