package com.app.privacyscreendisplay.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PrivacyGuardLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLightContainer,
    onPrimaryContainer = Color(0xFF14532D),
    secondary = EmeraldPrimaryDark,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryDark,
    surface = BackgroundLight,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryMuted,
    outline = BorderLight
)

/**
 * Main application theme enforcing the clean light mode design specified in screenshots.
 */
@Composable
fun PrivacyScreendisplayTheme(
    darkTheme: Boolean = false, // Forced light theme matching design screenshot
    dynamicColor: Boolean = false, // Disable wallpaper color overrides
    content: @Composable () -> Unit
) {
    val colorScheme = PrivacyGuardLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}