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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToSettings -> onNavigateToSettings()
                is HomeUiEvent.NavigateToProtectedApps -> onNavigateToProtectedApps()
                is HomeUiEvent.NavigateToActivityLog -> onNavigateToActivityLog()
                is HomeUiEvent.NavigateToPremiumPurchase -> onNavigateToPremium()
                is HomeUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onToggleProtection = viewModel::onToggleProtectionClicked,
        onStyleSelected = viewModel::onOverlayStyleSelected,
        onSensitivitySelected = viewModel::onSensitivitySelected,
        onProtectedAppsClick = viewModel::onProtectedAppsClicked,
        onActivityLogClick = viewModel::onActivityLogClicked,
        onPremiumClick = viewModel::onPremiumBannerClicked,
        onSettingsClick = viewModel::onSettingsClicked,
        modifier = modifier
    )
}

/**
 * Stateless Home Screen content layout composed with Material 3 styling.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
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

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Upgrade Premium",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(22.dp)
                    )
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
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                QuickSettingCard(
                    title = "AI Detection Sensitivity",
                    subtitle = "Required confidence threshold for extra face trigger",
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

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Activity Timeline Log Summary Card
                ActivityTimelinePreviewCard(
                    detectionsTodayCount = status.detectionsToday,
                    onViewLogClick = onActivityLogClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Premium Subscription Upgrade Card
                if (!status.isPremiumSubscriber) {
                    Card(
                        onClick = onPremiumClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFFC084FC))
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Upgrade to Privacy Guard PRO",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Unlock Gradient & Minimal overlays, zero ads & unlimited protection.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
