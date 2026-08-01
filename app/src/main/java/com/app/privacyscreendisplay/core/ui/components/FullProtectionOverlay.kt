package com.app.privacyscreendisplay.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.app.privacyscreendisplay.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle

/**
 * Pixel-perfect Full-Screen Privacy Protection Overlay matching Screenshot #9.
 * Reuses the exact same logo badge, text, and dismiss button across all 5 visual overlay styles,
 * allowing background customization (BLUR, GLASS, DARK, GRADIENT, MINIMAL) while keeping
 * 100% consistent typography and branding.
 */
@Composable
fun FullProtectionOverlay(
    isVisible: Boolean,
    overlayStyle: OverlayStyle = OverlayStyle.BLUR,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.96f),
        exit = fadeOut() + scaleOut(targetScale = 0.96f),
        modifier = modifier.fillMaxSize()
    ) {
        val textShadow = Shadow(
            color = Color.Black.copy(alpha = 0.5f),
            offset = Offset(0f, 2f),
            blurRadius = 8f
        )

        // Select background styling while keeping central logo & text 100% consistent
        val backgroundModifier = when (overlayStyle) {
            OverlayStyle.BLUR -> Modifier.background(Color(0xFF64748B)) // Solid 100% opaque slate grey
            OverlayStyle.GLASS -> Modifier.background(Color(0xFF0F172A).copy(alpha = 0.96f))
            OverlayStyle.DARK -> Modifier.background(Color(0xFF020617)) // Pitch black OLED mode
            OverlayStyle.GRADIENT -> Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF4338CA),
                        Color(0xFF6D28D9)
                    )
                )
            )
            OverlayStyle.MINIMAL -> Modifier.background(Color(0xFF1E293B))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier),
            contentAlignment = Alignment.Center
        ) {
            // Optional glassmorphism border card for GLASS style
            if (overlayStyle == OverlayStyle.GLASS) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                        .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(32.dp))
                        .padding(vertical = 36.dp, horizontal = 24.dp)
                ) {
                    ProtectionOverlayContent(onDismiss = onDismiss, textShadow = textShadow)
                }
            } else {
                ProtectionOverlayContent(onDismiss = onDismiss, textShadow = textShadow)
            }
        }
    }
}

/**
 * Reusable Central Overlay Content Component.
 * Contains the exact elevated white circular logo badge, red shoulder surfer icon,
 * headline ("Someone else is looking"), subtitle ("Screen is protected for your privacy"),
 * and light grey dismiss pill button.
 */
@Composable
private fun ProtectionOverlayContent(
    onDismiss: () -> Unit,
    textShadow: Shadow
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        // Central Logo Badge Image Asset (matching screenshot #9)
        Image(
            painter = painterResource(id = R.drawable.ic_shoulder_surfer),
            contentDescription = "Privacy Guard Logo Badge",
            modifier = Modifier
                .size(112.dp)
                .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.25f))
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Headline: "Someone else is looking"
        Text(
            text = "Someone else is\nlooking",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
            style = TextStyle(shadow = textShadow)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle: "Screen is protected for your privacy"
        Text(
            text = "Screen is protected for your privacy",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = textShadow)
        )

        Spacer(modifier = Modifier.height(34.dp))

        // Light Grey Dismiss Pill Button matching screenshot #9
        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF1F5F9).copy(alpha = 0.94f) // Light grey pill
            ),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Dismiss",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

/**
 * Custom Canvas vector drawing for the Shoulder Surfer Dual-Person Icon.
 */
@Composable
fun ShoulderSurferIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE11D48)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokeWidth = w * 0.085f

        // Main primary person head (Left figure)
        val mainHeadRadius = w * 0.17f
        val mainHeadCenter = Offset(w * 0.38f, h * 0.35f)
        drawCircle(
            color = color,
            radius = mainHeadRadius,
            center = mainHeadCenter,
            style = Stroke(width = strokeWidth)
        )

        // Main primary person body curve
        val mainBodyPath = Path().apply {
            moveTo(w * 0.16f, h * 0.76f)
            cubicTo(
                w * 0.16f, h * 0.56f,
                w * 0.60f, h * 0.56f,
                w * 0.60f, h * 0.76f
            )
        }
        drawPath(
            path = mainBodyPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Secondary shoulder surfer head (Right figure)
        val secHeadRadius = w * 0.14f
        val secHeadCenter = Offset(w * 0.68f, h * 0.39f)
        drawCircle(
            color = color,
            radius = secHeadRadius,
            center = secHeadCenter,
            style = Stroke(width = strokeWidth)
        )

        // Secondary shoulder surfer body curve
        val secBodyPath = Path().apply {
            moveTo(w * 0.50f, h * 0.76f)
            cubicTo(
                w * 0.50f, h * 0.60f,
                w * 0.86f, h * 0.60f,
                w * 0.86f, h * 0.76f
            )
        }
        drawPath(
            path = secBodyPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}
