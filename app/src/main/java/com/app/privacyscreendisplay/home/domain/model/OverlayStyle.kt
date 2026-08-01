package com.app.privacyscreendisplay.home.domain.model

/**
 * Enumeration representing available privacy screen overlay visual styles.
 *
 * @property displayName User-facing name of the overlay style.
 * @property description Brief summary of how the overlay renders over sensitive content.
 * @property isPremium Indicates whether this overlay style requires a Premium subscription.
 */
enum class OverlayStyle(
    val displayName: String,
    val description: String,
    val isPremium: Boolean
) {
    BLUR(
        displayName = "Blur",
        description = "Frosted glass gaussian blur filter",
        isPremium = false
    ),
    GLASS(
        displayName = "Glass",
        description = "Translucent acrylic dark frost",
        isPremium = false
    ),
    DARK(
        displayName = "Dark",
        description = "Opaque dark privacy mask",
        isPremium = false
    ),
    GRADIENT(
        displayName = "Gradient",
        description = "Vibrant privacy color mesh",
        isPremium = true
    ),
    MINIMAL(
        displayName = "Minimal",
        description = "Subtle top-screen privacy indicator strip",
        isPremium = true
    )
}
