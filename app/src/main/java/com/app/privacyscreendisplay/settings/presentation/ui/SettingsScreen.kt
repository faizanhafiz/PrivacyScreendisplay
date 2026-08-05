package com.app.privacyscreendisplay.settings.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.app.privacyscreendisplay.core.ads.AdConfig
import com.app.privacyscreendisplay.core.ads.AdMobBannerView
import com.app.privacyscreendisplay.core.monitor.ForegroundAppMonitor
import com.app.privacyscreendisplay.core.ui.components.LocalToastState
import com.app.privacyscreendisplay.core.ui.components.ToastType
import com.app.privacyscreendisplay.home.data.datasource.PrivacyGuardLocalDataSource
import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import kotlinx.coroutines.launch

/**
 * Modern Settings Screen for Privacy Guard AI.
 * Provides full control over protection parameters, permissions status,
 * premium subscription waitlist, and app feedback.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToPermission: (String) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val toastState = LocalToastState.current
    val coroutineScope = rememberCoroutineScope()
    val localDataSource = remember(context) { PrivacyGuardLocalDataSource(context) }

    val status by localDataSource.protectionStatusFlow.collectAsState(
        initial = com.app.privacyscreendisplay.home.domain.model.ProtectionStatus(
            isProtectionActive = true,
            selectedOverlayStyle = com.app.privacyscreendisplay.home.domain.model.OverlayStyle.BLUR,
            sensitivity = SensitivityLevel.MEDIUM,
            protectedAppsCount = 0,
            detectionsToday = 0,
            isPremiumSubscriber = AdConfig.isPremiumUser,
            recentLogs = emptyList()
        )
    )

    val hasCameraPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    val hasOverlayPermission = remember(context) {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }
    val hasUsagePermission = remember(context) {
        ForegroundAppMonitor(context).hasUsageAccessPermission()
    }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))



            // 2. Premium Status / Upgrade Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigateToPremium() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        CrownIcon(modifier = Modifier.size(22.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (status.isPremiumSubscriber) "Privacy Guard Pro Active" else "Go Premium / VIP Waitlist",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = if (status.isPremiumSubscriber) "All ad-free & AI features unlocked" else "Join VIP early access & remove ads",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFFD97706)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Banner Ad Container (Suppressed for Premium Users)
            if (!status.isPremiumSubscriber) {
                AdMobBannerView(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 4. Protection & AI Settings Group
            SettingsSectionHeader(title = "AI PROTECTION CONTROLS")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    // Active Toggle Row
                    SettingsToggleRow(
                        icon = Icons.Rounded.Lock,
                        iconTint = Color(0xFF2563EB),
                        title = "Master Shield Protection",
                        subtitle = "Enable real-time AI camera detection",
                        isChecked = status.isProtectionActive,
                        onCheckedChange = { active ->
                            coroutineScope.launch {
                                localDataSource.setProtectionActive(active)
                                toastState.show(
                                    if (active) "Protection Shield Activated" else "Protection Shield Deactivated",
                                    if (active) ToastType.SUCCESS else ToastType.WARNING
                                )
                            }
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    // AI Sensitivity Selector Row
                    SettingsActionRow(
                        icon = Icons.Rounded.Psychology,
                        iconTint = Color(0xFF7C3AED),
                        title = "AI Detection Sensitivity",
                        valueText = status.sensitivity.name.lowercase().capitalize(),
                        onClick = {
                            val nextSensitivity = when (status.sensitivity) {
                                SensitivityLevel.LOW -> SensitivityLevel.MEDIUM
                                SensitivityLevel.MEDIUM -> SensitivityLevel.HIGH
                                SensitivityLevel.HIGH -> SensitivityLevel.LOW
                            }
                            coroutineScope.launch {
                                localDataSource.setSensitivityLevel(nextSensitivity)
                                toastState.show("Sensitivity updated to ${nextSensitivity.name}", ToastType.INFO)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. System Permissions Health Group
            SettingsSectionHeader(title = "REQUIRED PERMISSIONS STATUS")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    // Camera Permission Row
                    PermissionStatusRow(
                        icon = Icons.Rounded.CameraAlt,
                        title = "Camera Access",
                        subtitle = "Required for AI shoulder surfing scanning",
                        isGranted = hasCameraPermission,
                        onClick = { if (!hasCameraPermission) onNavigateToPermission("permission_camera") }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    // Display Overlay Row
                    PermissionStatusRow(
                        icon = Icons.Rounded.Layers,
                        title = "Display Over Other Apps",
                        subtitle = "Required to render privacy blur shield",
                        isGranted = hasOverlayPermission,
                        onClick = { if (!hasOverlayPermission) onNavigateToPermission("permission_overlay") }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    // Usage Access Row
                    PermissionStatusRow(
                        icon = Icons.Rounded.Lock,
                        title = "Usage Access",
                        subtitle = "Required to detect protected app launches",
                        isGranted = hasUsagePermission,
                        onClick = { if (!hasUsagePermission) onNavigateToPermission("permission_usage_access") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Support & Feedback Group
            SettingsSectionHeader(title = "SUPPORT & ABOUT")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {


                    SettingsActionRow(
                        icon = Icons.Rounded.StarRate,
                        iconTint = Color(0xFFF59E0B),
                        title = "Rate",
                        valueText = "",
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                            } catch (_: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                            }
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsActionRow(
                        icon = Icons.Rounded.SupportAgent,
                        iconTint = Color(0xFF10B981),
                        title = "Contact Support",
                        valueText = "Feedback",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:neuraai.apps@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Privacy Guard AI Support Query")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                toastState.show("No email app found", ToastType.WARNING)
                            }
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsActionRow(
                        icon = Icons.Rounded.Shield,
                        iconTint = Color(0xFF6366F1),
                        title = "Privacy Policy",
                        valueText = "",
                        onClick = onNavigateToPrivacyPolicy
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Version Info
            Text(
                text = "Privacy Guard AI v1.0.0 (Build 100)\nOn-Device Real-Time Vision Intelligence",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB)
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    valueText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))

        if (valueText.isNotEmpty()) {
            Text(text = valueText, fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
        }

        Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
    }
}

@Composable
private fun PermissionStatusRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isGranted) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF16A34A) else Color(0xFFDC2626),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
        }

        Text(
            text = if (isGranted) "Granted" else "Action Required",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) Color(0xFF16A34A) else Color(0xFFDC2626)
        )
    }
}

@Composable
private fun CrownIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val goldenBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
        )

        val crownPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.35f)
            lineTo(w * 0.30f, h * 0.65f)
            lineTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.70f, h * 0.65f)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.80f, h * 0.82f)
            cubicTo(w * 0.65f, h * 0.88f, w * 0.35f, h * 0.88f, w * 0.20f, h * 0.82f)
            close()
        }
        drawPath(crownPath, goldenBrush)
        drawCircle(Color(0xFFFEF08A), radius = w * 0.055f, center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.35f))
        drawCircle(Color(0xFFFEF08A), radius = w * 0.065f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.22f))
        drawCircle(Color(0xFFFEF08A), radius = w * 0.055f, center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.35f))
    }
}

private fun String.capitalize(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
