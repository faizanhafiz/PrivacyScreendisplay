package com.app.privacyscreendisplay.setup.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pixel-perfect Setup Required / Permission Intro Screen matching Image 1.
 * Note: Accessibility Service has been removed from the list as requested.
 */
@Composable
fun PermissionSetupIntroScreen(
    onStartSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Green Shield with Keyhole Icon Header
                ShieldHeaderIcon(modifier = Modifier.size(88.dp))

                Spacer(modifier = Modifier.height(20.dp))

                // Title: Let's Set Up Protection
                Text(
                    text = "Let's Set Up Protection",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "To provide complete protection,\nwe need the following permissions.",
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Permission List Items (Accessibility removed as requested)
                PermissionIntroCard(
                    icon = Icons.Rounded.CameraAlt,
                    title = "Camera",
                    description = "Detect people looking\nat your screen.",
                    onClick = onStartSetupClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                PermissionIntroCard(
                    icon = Icons.Rounded.Layers,
                    title = "Display over other apps",
                    description = "Blur your screen when\nsomeone is watching.",
                    onClick = onStartSetupClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                PermissionIntroCard(
                    icon = Icons.Rounded.BarChart,
                    title = "Usage Access",
                    description = "Detect when apps are\nopened for protection.",
                    onClick = onStartSetupClick
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Primary Action Button: Start Setup
            Button(
                onClick = onStartSetupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF047857)
                )
            ) {
                Text(
                    text = "Start Setup",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionIntroCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Light Green Circle Icon Container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                lineHeight = 17.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ShieldHeaderIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Outer Shield Shape
            val shieldPath = Path().apply {
                moveTo(w * 0.5f, 0f)
                cubicTo(
                    w * 0.85f, h * 0.1f,
                    w * 1.0f, h * 0.25f,
                    w * 1.0f, h * 0.5f
                )
                cubicTo(
                    w * 1.0f, h * 0.82f,
                    w * 0.65f, h * 0.98f,
                    w * 0.5f, h * 1.0f
                )
                cubicTo(
                    w * 0.35f, h * 0.98f,
                    0f, h * 0.82f,
                    0f, h * 0.5f
                )
                cubicTo(
                    0f, h * 0.25f,
                    w * 0.15f, h * 0.1f,
                    w * 0.5f, 0f
                )
                close()
            }

            drawPath(shieldPath, Color(0xFF047857))

            // Inner White Lock Body
            val lockBodyWidth = w * 0.28f
            val lockBodyHeight = h * 0.26f
            val lockBodyLeft = (w - lockBodyWidth) / 2f
            val lockBodyTop = h * 0.50f

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(lockBodyLeft, lockBodyTop),
                size = androidx.compose.ui.geometry.Size(lockBodyWidth, lockBodyHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f, w * 0.05f)
            )

            // Inner Lock Shackle
            val shackleRadius = w * 0.11f
            drawCircle(
                color = Color.White,
                radius = shackleRadius,
                center = Offset(w * 0.5f, h * 0.44f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.07f)
            )

            // Keyhole center hole
            drawCircle(
                color = Color(0xFF047857),
                radius = w * 0.04f,
                center = Offset(w * 0.5f, h * 0.60f)
            )
        }
    }
}
