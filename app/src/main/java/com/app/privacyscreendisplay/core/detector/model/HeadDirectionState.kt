package com.app.privacyscreendisplay.core.detector.model

/**
 * Head direction classification relative to the phone's screen space.
 */
enum class HeadDirectionState(
    val confidenceWeight: Float,
    val description: String
) {
    /**
     * Head is oriented directly toward the phone screen.
     */
    LOOKING_AT_SCREEN(1.00f, "Facing phone screen directly"),

    /**
     * Head orientation is angled slightly toward the phone screen.
     */
    PROBABLY_LOOKING(0.75f, "Angled toward screen"),

    /**
     * Neutral head angle (e.g. forward, but not aligned with screen vector).
     */
    NEUTRAL(0.35f, "Neutral head orientation"),

    /**
     * Head is turned away from the phone screen.
     */
    LOOKING_AWAY(0.00f, "Turned away from screen"),

    /**
     * Head is pointing completely elsewhere (e.g. looking down at own phone, ceiling, or floor).
     */
    DEFINITELY_ELSEWHERE(0.00f, "Looking elsewhere")
}
