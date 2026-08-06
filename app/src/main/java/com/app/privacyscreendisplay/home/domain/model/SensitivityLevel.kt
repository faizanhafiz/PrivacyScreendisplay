package com.app.privacyscreendisplay.home.domain.model

/**
 * Represents AI Face Detection sensitivity thresholds and camera sampling intervals.
 * Couples detection confidence with battery and status bar camera indicator duration.
 *
 * @property displayName Label displayed to the user.
 * @property confidenceThreshold Required ML Kit confidence score (0.0 to 1.0).
 * @property scanDurationMs Duration camera stays active per sampling cycle.
 * @property pauseDurationMs Duration camera disarms between sampling cycles (clearing green dot).
 * @property subtitleDescription Explanatory subtitle for home screen UI.
 */
enum class SensitivityLevel(
    val displayName: String,
    val confidenceThreshold: Float,
    val scanDurationMs: Long,
    val pauseDurationMs: Long,
    val subtitleDescription: String
) {
    LOW(
        displayName = "Low (Eco)",
        confidenceThreshold = 0.85f,
        scanDurationMs = 1500L,
        pauseDurationMs = 5000L,
        subtitleDescription = "Eco Mode"
    ),
    MEDIUM(
        displayName = "Medium",
        confidenceThreshold = 0.70f,
        scanDurationMs = 2000L,
        pauseDurationMs = 3000L,
        subtitleDescription = "Balanced"
    ),
    HIGH(
        displayName = "High",
        confidenceThreshold = 0.50f,
        scanDurationMs = Long.MAX_VALUE,
        pauseDurationMs = 0L,
        subtitleDescription = "Real-time"
    )
}
