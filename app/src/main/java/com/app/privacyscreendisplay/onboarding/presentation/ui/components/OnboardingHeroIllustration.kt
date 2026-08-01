package com.app.privacyscreendisplay.onboarding.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Recreates the exact high-fidelity vector hero graphic from the design screenshot:
 * Features a 3D folded Emerald Green privacy shield with camera lens eye,
 * outer orbit rings with node markers, and an overlapping secondary avatar bubble.
 */
@Composable
fun OnboardingHeroIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Concentric Orbit Rings & Nodes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringRadiusInner = size.width * 0.40f
            val ringRadiusOuter = size.width * 0.47f

            // Inner & Outer Green Orbit Circles
            drawCircle(
                color = Color(0xFF86EFAC).copy(alpha = 0.35f),
                radius = ringRadiusOuter,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFBBF7D0).copy(alpha = 0.45f),
                radius = ringRadiusInner,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Green Decorative Node Dots
            drawCircle(
                color = Color(0xFF10B981),
                radius = 5.dp.toPx(),
                center = Offset(
                    center.x - ringRadiusInner * 0.95f,
                    center.y - ringRadiusInner * 0.05f
                )
            )
            drawCircle(
                color = Color(0xFF34D399),
                radius = 4.dp.toPx(),
                center = Offset(
                    center.x + ringRadiusOuter * 0.70f,
                    center.y - ringRadiusOuter * 0.65f
                )
            )
            drawCircle(
                color = Color(0xFF6EE7B7),
                radius = 3.dp.toPx(),
                center = Offset(
                    center.x - ringRadiusOuter * 0.50f,
                    center.y + ringRadiusOuter * 0.75f
                )
            )
        }

        // 3D Folded Emerald Green Privacy Shield
        Box(
            modifier = Modifier.size(136.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Full Shield Path
                val shieldPathLeft = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    cubicTo(w * 0.15f, h * 0.05f, 0f, h * 0.15f, 0f, h * 0.38f)
                    cubicTo(0f, h * 0.72f, w * 0.35f, h * 0.93f, w * 0.5f, h)
                    close()
                }

                val shieldPathRight = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    cubicTo(w * 0.85f, h * 0.05f, w, h * 0.15f, w, h * 0.38f)
                    cubicTo(w, h * 0.72f, w * 0.65f, h * 0.93f, w * 0.5f, h)
                    close()
                }

                // Left Half Gradient (Lighter Emerald)
                drawPath(
                    path = shieldPathLeft,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF34D399), Color(0xFF10B981))
                    )
                )

                // Right Half Gradient (Darker Forest Green for 3D Bevel Fold)
                drawPath(
                    path = shieldPathRight,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF059669), Color(0xFF047857))
                    )
                )
            }

            // Central Camera Lens Eye Container
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dark Pupil Core
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF374151), Color(0xFF111827))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Pupil White Glare Highlight Dot
                    Box(
                        modifier = Modifier
                            .offset(x = (-5).dp, y = (-5).dp)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                    )
                }
            }
        }

        // Overlapping Secondary Face Avatar Badge (Bottom Right)
        Box(
            modifier = Modifier
                .offset(x = 44.dp, y = 30.dp)
                .size(56.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFFC7D2FE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color(0xFF4338CA),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
