package com.app.privacyscreendisplay.core.ui.components

import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Completely Transparent Full-Screen Ad Loading Overlay.
 * Eliminates all background dimming and white cards - ONLY renders the 3-dot blue animated loader on top of the current screen.
 */
@Composable
fun AdLoadingOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = {}, // Non-dismissible while ad is loading
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            ThreeDotTrianglePulseLoader()
        }
    }
}

/**
 * Custom 3-Dot Triangular Pulsing Loader Component.
 * Formed by 1 top dot and 2 bottom dots in a triangular layout matching the exact design.
 */
@Composable
fun ThreeDotTrianglePulseLoader(
    modifier: Modifier = Modifier,
    dotSize: Float = 26f,
    activeColor: Color = Color(0xFF2563EB),  // Vibrant Royal Blue
    inactiveColor: Color = Color(0xFFBFDBFE) // Light Soft Blue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ThreeDotPulseTransition")

    // Animates cycle phase smoothly from 0f to 3f
    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DotPhaseAnimation"
    )

    // Calculate individual scale & alpha for each of the 3 dots based on animationPhase
    val topDotScale = getDotScale(animationPhase, 0f)
    val topDotAlpha = getDotAlpha(animationPhase, 0f)

    val bottomLeftDotScale = getDotScale(animationPhase, 1f)
    val bottomLeftDotAlpha = getDotAlpha(animationPhase, 1f)

    val bottomRightDotScale = getDotScale(animationPhase, 2f)
    val bottomRightDotAlpha = getDotAlpha(animationPhase, 2f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top Dot (Dot 1)
        Box(
            modifier = Modifier
                .size(dotSize.dp)
                .scale(topDotScale)
                .alpha(topDotAlpha)
                .clip(CircleShape)
                .background(if (topDotScale > 1.1f) activeColor else inactiveColor)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom Row: Bottom-Left Dot (Dot 2) & Bottom-Right Dot (Dot 3)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize.dp)
                    .scale(bottomLeftDotScale)
                    .alpha(bottomLeftDotAlpha)
                    .clip(CircleShape)
                    .background(if (bottomLeftDotScale > 1.1f) activeColor else inactiveColor)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(dotSize.dp)
                    .scale(bottomRightDotScale)
                    .alpha(bottomRightDotAlpha)
                    .clip(CircleShape)
                    .background(if (bottomRightDotScale > 1.1f) activeColor else inactiveColor)
            )
        }
    }
}

private fun getDotScale(currentPhase: Float, targetPhaseIndex: Float): Float {
    val distance = Math.abs((currentPhase - targetPhaseIndex) % 3f)
    val normalized = if (distance > 1.5f) 3f - distance else distance
    return 0.9f + (1.0f - (normalized / 1.5f).coerceIn(0f, 1f)) * 0.35f
}

private fun getDotAlpha(currentPhase: Float, targetPhaseIndex: Float): Float {
    val distance = Math.abs((currentPhase - targetPhaseIndex) % 3f)
    val normalized = if (distance > 1.5f) 3f - distance else distance
    return 0.6f + (1.0f - (normalized / 1.5f).coerceIn(0f, 1f)) * 0.40f
}
