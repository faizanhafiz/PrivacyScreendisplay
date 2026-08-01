package com.app.privacyscreendisplay.protectedapps.domain.model

/**
 * Domain representation of an application configured for automatic privacy blur.
 */
data class ProtectedApp(
    val packageName: String,
    val appName: String,
    val categoryName: String,
    val iconResName: String,
    val isProtected: Boolean = true
)
