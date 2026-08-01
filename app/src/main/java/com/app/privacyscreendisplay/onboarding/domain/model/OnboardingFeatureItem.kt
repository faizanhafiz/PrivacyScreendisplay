package com.app.privacyscreendisplay.onboarding.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Domain entity representing an individual feature highlight item displayed on the onboarding screen.
 *
 * @property id Unique identifier for the feature item.
 * @property title Concise title of the feature highlight.
 * @property description Explanatory subtitle text describing the functionality.
 * @property icon ImageVector icon representing the feature visually.
 */
data class OnboardingFeatureItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)
