package com.app.privacyscreendisplay.home.domain.model

/**
 * Represents AI Face Detection sensitivity thresholds for triggering the privacy overlay.
 *
 * @property displayName Label displayed to the user.
 * @property confidenceThreshold Required ML Kit confidence score (0.0 to 1.0).
 */
enum class SensitivityLevel(
    val displayName: String,
    val confidenceThreshold: Float
) {
    LOW(
        displayName = "Low",
        confidenceThreshold = 0.85f
    ),
    MEDIUM(
        displayName = "Medium",
        confidenceThreshold = 0.70f
    ),
    HIGH(
        displayName = "High",
        confidenceThreshold = 0.50f
    )
}
