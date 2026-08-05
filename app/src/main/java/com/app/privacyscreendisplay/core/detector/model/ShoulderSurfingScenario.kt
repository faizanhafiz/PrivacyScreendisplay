package com.app.privacyscreendisplay.core.detector.model

/**
 * High-level detection scenario results evaluated by the Shoulder Surfing Engine.
 */
enum class ShoulderSurfingScenario(
    val description: String,
    val isAlertRequired: Boolean
) {
    /**
     * No shoulder surfing threat detected. (e.g. Single user, friends looking away/at own phone, walking past).
     */
    SAFE("Environment is safe. No active shoulder surfers.", false),

    /**
     * Suspicious behavior detected (e.g. Repeated glances from 1.5m). Requires continued observation.
     */
    MEDIUM_RISK("Potential shoulder surfing glances detected.", false),

    /**
     * Confirmed shoulder surfing threat! Another person is continuously watching the user's screen.
     */
    ALERT("Shoulder surfing detected! Screen privacy protection activated.", true)
}
