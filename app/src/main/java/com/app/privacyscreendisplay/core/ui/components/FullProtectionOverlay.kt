package com.app.privacyscreendisplay.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle

/**
 * Pixel-perfect Full-Screen Privacy Protection Overlay matching Screenshot #9.
 * Replicates the translucent frosted grey blur backdrop, central white circle with
 * custom red dual-person shoulder surfer alert icon, bold headline, subtitle, and
 * light grey dismiss pill button.
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
        // Translucent frosted grey blur backdrop matching exact screenshot appearance
        val backdropColor = when (overlayStyle) {
            OverlayStyle.BLUR -> Color(0xFF0F172A).copy(alpha = 0.35f) // Translucent over blurred content
            OverlayStyle.GLASS -> Color(0xFF0F172A).copy(alpha = 0.45f)
            OverlayStyle.DARK -> Color(0xFF020617).copy(alpha = 0.95f)
            OverlayStyle.GRADIENT -> Color(0xFF0F172A).copy(alpha = 0.70f)
            OverlayStyle.MINIMAL -> Color(0xFF1E293B).copy(alpha = 0.30f)
        }

        val textShadow = Shadow(
            color = Color.Black.copy(alpha = 0.5f),
            offset = Offset(0f, 2f),
            blurRadius = 8f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backdropColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Central White Circle Container (matching screenshot)
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom Red Outlined Shoulder Surfer Dual-Person Badge Icon
                    ShoulderSurferIcon(
                        modifier = Modifier.size(54.dp),
                        color = Color(0xFFE11D48) // Vibrant Red matching screenshot
                    )
                }

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

                // Light Grey Dismiss Pill Button matching screenshot
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas vector drawing of the two outlined red figures with detection alert badge
 * matching Screenshot #9.
 */
@Composable
private fun ShoulderSurferIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE11D48)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 3.2.dp.toPx()

        // --- Left Person (Primary User) ---
        // Left Head
        drawCircle(
            color = color,
            radius = width * 0.12f,
            center = Offset(width * 0.35f, height * 0.30f),
            style = Stroke(width = strokeWidth)
        )

        // Left Body Arc
        val leftBodyPath = Path().apply {
            moveTo(width * 0.16f, height * 0.72f)
            cubicTo(
                width * 0.16f, height * 0.50f,
                width * 0.54f, height * 0.50f,
                width * 0.54f, height * 0.72f
            )
        }
        drawPath(
            path = leftBodyPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // --- Right Person (Shoulder Surfer Alert) ---
        // Right Head
        drawCircle(
            color = color,
            radius = width * 0.12f,
            center = Offset(width * 0.65f, height * 0.32f),
            style = Stroke(width = strokeWidth)
        )

        // Right Head Alert Dot/Badge
        drawCircle(
            color = color,
            radius = width * 0.045f,
            center = Offset(width * 0.76f, height * 0.18f)
        )

        // Right Body Arc
        val rightBodyPath = Path().apply {
            moveTo(width * 0.46f, height * 0.72f)
            cubicTo(
                width * 0.46f, height * 0.50f,
                width * 0.84f, height * 0.50f,
                width * 0.84f, height * 0.72f
            )
        }
        drawPath(
            path = rightBodyPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}
