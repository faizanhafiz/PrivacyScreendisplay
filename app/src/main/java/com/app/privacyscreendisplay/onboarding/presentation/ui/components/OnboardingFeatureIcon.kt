package com.app.privacyscreendisplay.onboarding.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Custom Canvas Vector Icon Renderer generating exact custom vector icons matching the design screenshot:
 * 1. AI Face Detection: Circular scan ring with checkmark
 * 2. Instant Screen Protection: Document shield container with checkmark
 * 3. Works Across Apps: 4-node app mesh grid cluster
 */
@Composable
fun OnboardingFeatureIcon(
    featureId: String,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF1B8755)
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        when (featureId) {
            "feature_ai_detection" -> {
                // Outer Circle Ring with bottom check accent
                drawCircle(
                    color = tint,
                    radius = w * 0.42f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = strokeWidth)
                )

                // Inner Checkmark Path
                val checkPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.52f)
                    lineTo(w * 0.46f, h * 0.64f)
                    lineTo(w * 0.65f, h * 0.38f)
                }
                drawPath(
                    path = checkPath,
                    color = tint,
                    style = Stroke(width = strokeWidth * 1.1f)
                )
            }

            "feature_instant_protection" -> {
                // Rounded Document/Shield Card Shape
                val cardWidth = w * 0.65f
                val cardHeight = h * 0.78f
                val left = (w - cardWidth) / 2f
                val top = (h - cardHeight) / 2f

                drawRoundRect(
                    color = tint,
                    topLeft = Offset(left, top),
                    size = Size(cardWidth, cardHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = strokeWidth)
                )

                // Center Checkmark Path
                val checkPath = Path().apply {
                    moveTo(w * 0.38f, h * 0.50f)
                    lineTo(w * 0.47f, h * 0.60f)
                    lineTo(w * 0.62f, h * 0.42f)
                }
                drawPath(
                    path = checkPath,
                    color = tint,
                    style = Stroke(width = strokeWidth * 1.1f)
                )
            }

            "feature_cross_app" -> {
                // 4 App Node Circles in 2x2 Grid with connecting line
                val radius = w * 0.14f

                // Top-Left Node
                drawCircle(color = tint, radius = radius, center = Offset(w * 0.32f, h * 0.32f))
                // Top-Right Node
                drawCircle(color = tint, radius = radius, center = Offset(w * 0.68f, h * 0.32f), style = Stroke(width = strokeWidth))
                // Bottom-Left Node
                drawCircle(color = tint, radius = radius, center = Offset(w * 0.32f, h * 0.68f), style = Stroke(width = strokeWidth))
                // Bottom-Right Node (User / App Node)
                drawCircle(color = tint, radius = radius, center = Offset(w * 0.68f, h * 0.68f))

                // Linking path line
                drawLine(
                    color = tint,
                    start = Offset(w * 0.32f, h * 0.32f),
                    end = Offset(w * 0.68f, h * 0.68f),
                    strokeWidth = strokeWidth * 0.8f
                )
            }

            else -> {
                // Fallback circle check
                drawCircle(
                    color = tint,
                    radius = w * 0.4f,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}
