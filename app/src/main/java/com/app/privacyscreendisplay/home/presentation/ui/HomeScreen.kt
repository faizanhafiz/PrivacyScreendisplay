package com.app.privacyscreendisplay.home.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.privacyscreendisplay.core.ads.AdMobBottomSheetAdDialog
import com.app.privacyscreendisplay.core.ui.components.FullProtectionOverlay
import com.app.privacyscreendisplay.core.ui.components.LocalToastState
import com.app.privacyscreendisplay.core.ui.components.ToastType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import com.app.privacyscreendisplay.home.presentation.ui.components.ActivityTimelinePreviewCard
import com.app.privacyscreendisplay.home.presentation.ui.components.OverlayStyleSelector
import com.app.privacyscreendisplay.home.presentation.ui.components.ProtectionStatusCard
import com.app.privacyscreendisplay.home.presentation.ui.components.QuickSettingCard
import com.app.privacyscreendisplay.home.presentation.viewmodel.HomeUiEvent
import com.app.privacyscreendisplay.home.presentation.viewmodel.HomeUiState
import com.app.privacyscreendisplay.home.presentation.viewmodel.HomeViewModel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.app.privacyscreendisplay.core.monitor.ForegroundAppMonitor

/**
 * Stateful entry point for the Privacy Guard Home Screen.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToProtectedApps: () -> Unit,
    onNavigateToActivityLog: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToPermission: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastState = LocalToastState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToSettings -> onNavigateToSettings()
                is HomeUiEvent.NavigateToProtectedApps -> onNavigateToProtectedApps()
                is HomeUiEvent.NavigateToActivityLog -> onNavigateToActivityLog()
                is HomeUiEvent.NavigateToPremiumPurchase -> onNavigateToPremium()
                is HomeUiEvent.ShowToast -> {
                    toastState.show(event.message)
                }
            }
        }
    }

    val onToggleWithPermissionCheck = {
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
        val hasUsageAccess = ForegroundAppMonitor(context).hasUsageAccessPermission()

        if (!hasCamera) {
            toastState.show("Camera permission required to activate protection", ToastType.WARNING)
            onNavigateToPermission("permission_camera")
        } else if (!hasOverlay) {
            toastState.show("Display over other apps permission required to activate protection", ToastType.WARNING)
            onNavigateToPermission("permission_overlay")
        } else if (!hasUsageAccess) {
            toastState.show("Usage Access permission required to activate protection", ToastType.WARNING)
            onNavigateToPermission("permission_usage_access")
        } else {
            viewModel.onToggleProtectionClicked()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        shouldShowLaunchAd = viewModel::shouldShowLaunchAd,
        onToggleProtection = onToggleWithPermissionCheck,
        onStyleSelected = viewModel::onOverlayStyleSelected,
        onSensitivitySelected = viewModel::onSensitivitySelected,
        onProtectedAppsClick = onNavigateToProtectedApps,
        onActivityLogClick = onNavigateToActivityLog,
        onPremiumClick = onNavigateToPremium,
        onSettingsClick = onNavigateToSettings,
        modifier = modifier
    )
}

/**
 * Stateless Home Screen content layout composed with Material 3 styling.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    shouldShowLaunchAd: () -> Boolean = { false },
    onToggleProtection: () -> Unit,
    onStyleSelected: (OverlayStyle) -> Unit,
    onSensitivitySelected: (SensitivityLevel) -> Unit,
    onProtectedAppsClick: () -> Unit,
    onActivityLogClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = uiState.protectionStatus
    var showBottomSheetAd by remember { mutableStateOf(false) }
    var showProtectionOverlay by remember { mutableStateOf(false) }

    // Sync global AdConfig premium flag
    LaunchedEffect(status.isPremiumSubscriber) {
        com.app.privacyscreendisplay.core.ads.AdConfig.isPremiumUser = status.isPremiumSubscriber
    }

    // Trigger Bottom Sheet Modal Dialog Ad ONLY on initial app launch if NOT premium
    LaunchedEffect(Unit) {
        if (!status.isPremiumSubscriber && shouldShowLaunchAd()) {
            kotlinx.coroutines.delay(5000L) // 5-second delay to prevent overlap with App Open Ad
            if (!status.isPremiumSubscriber) {
                showBottomSheetAd = true
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (showProtectionOverlay) 25.dp else 0.dp)
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

            // Header TopBar (Scrolls together with screen content)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Privacy Guard",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Active Green Status Dot Badge
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (status.isProtectionActive) Color(0xFF10B981) else Color(0xFF6B7280))
                        )
                    }

                    Text(
                        text = "Real-time AI Shoulder Surfing Defense",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Premium Upgrade Button Action
                IconButton(
                    onClick = onPremiumClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7))
                ) {
                    CrownIcon(modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Settings Action Button
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Hero Protection Banner Switch Card
                ProtectionStatusCard(
                    status = status,
                    onToggleProtection = onToggleProtection
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Overlay Style Selector Component
                OverlayStyleSelector(
                    selectedStyle = status.selectedOverlayStyle,
                    onStyleSelected = onStyleSelected
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Inline Ad Placement right after Protected Apps card
                if (!status.isPremiumSubscriber) {
                    com.app.privacyscreendisplay.home.presentation.ui.components.AdBannerCard(
                        onAdClick = onPremiumClick
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 3. Quick Settings Section
                Text(
                    text = "Quick Controls",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuickSettingCard(
                    title = "Protected Apps",
                    subtitle = "Apps configured for instant privacy blur",
                    icon = Icons.Rounded.Apps,
                    badgeText = "${status.protectedAppsCount} Active",
                    onClick = onProtectedAppsClick,
                    iconTint = Color(0xFF16A34A)
                )

                Spacer(modifier = Modifier.height(14.dp))



                QuickSettingCard(
                    title = "AI Detection Sensitivity",
                    subtitle = status.sensitivity.subtitleDescription,
                    icon = Icons.Rounded.Psychology,
                    badgeText = status.sensitivity.displayName,
                    onClick = {
                        val nextSensitivity = when (status.sensitivity) {
                            SensitivityLevel.LOW -> SensitivityLevel.MEDIUM
                            SensitivityLevel.MEDIUM -> SensitivityLevel.HIGH
                            SensitivityLevel.HIGH -> SensitivityLevel.LOW
                        }
                        onSensitivitySelected(nextSensitivity)
                    },
                    iconTint = Color(0xFF0284C7)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Activity Timeline Log Summary Card
                ActivityTimelinePreviewCard(
                    detectionsTodayCount = status.detectionsToday,
                    recentLogs = status.recentLogs,
                    onViewLogClick = onActivityLogClick
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Sheet Interstitial Native Dialog Ad Overlay (Suppressed for Premium Users)
        if (!status.isPremiumSubscriber) {
            AdMobBottomSheetAdDialog(
                isVisible = showBottomSheetAd,
                onDismiss = { showBottomSheetAd = false },
                onAdClick = onPremiumClick
            )
        }

        // Full Screen Privacy Protection Overlay (Triggered on shoulder surfing detection)
        FullProtectionOverlay(
            isVisible = showProtectionOverlay,
            overlayStyle = status.selectedOverlayStyle,
            onDismiss = { showProtectionOverlay = false }
        )
    }
}

/**
 * Custom Golden Crown Icon for top bar Premium upgrade button.
 */
@Composable
private fun CrownIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val goldenBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFBBF24),
                Color(0xFFD97706)
            )
        )

        val crownPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.35f)
            lineTo(w * 0.30f, h * 0.65f)
            lineTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.70f, h * 0.65f)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.80f, h * 0.82f)
            cubicTo(
                w * 0.65f, h * 0.88f,
                w * 0.35f, h * 0.88f,
                w * 0.20f, h * 0.82f
            )
            close()
        }
        drawPath(crownPath, goldenBrush)
        drawCircle(Color(0xFFFEF08A), radius = w * 0.055f, center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.35f))
        drawCircle(Color(0xFFFEF08A), radius = w * 0.065f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.22f))
        drawCircle(Color(0xFFFEF08A), radius = w * 0.055f, center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.35f))
    }
}
