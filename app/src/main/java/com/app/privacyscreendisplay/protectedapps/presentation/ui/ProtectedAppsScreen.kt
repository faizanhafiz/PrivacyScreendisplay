package com.app.privacyscreendisplay.protectedapps.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.privacyscreendisplay.home.presentation.ui.components.AdBannerCard
import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp
import com.app.privacyscreendisplay.protectedapps.presentation.ui.components.AddAppButton
import com.app.privacyscreendisplay.protectedapps.presentation.ui.components.AddedAppItemCard
import com.app.privacyscreendisplay.protectedapps.presentation.ui.components.PlanQuotaCard
import com.app.privacyscreendisplay.protectedapps.presentation.ui.components.UpgradePremiumPromptCard
import com.app.privacyscreendisplay.protectedapps.presentation.viewmodel.ProtectedAppsUiState
import com.app.privacyscreendisplay.protectedapps.presentation.viewmodel.ProtectedAppsViewModel

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.privacyscreendisplay.protectedapps.presentation.ui.components.AppPickerDialog

import com.app.privacyscreendisplay.core.ui.components.LocalToastState
import com.app.privacyscreendisplay.core.ui.components.ToastType

@Composable
fun ProtectedAppsScreen(
    viewModel: ProtectedAppsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastState = LocalToastState.current
    val context = LocalContext.current
    var showAppPickerDialog by remember { mutableStateOf(false) }
    var appPendingRemoval by remember { mutableStateOf<ProtectedApp?>(null) }
    var isAdLoading by remember { mutableStateOf(false) }

    ProtectedAppsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUpgradeClick = onNavigateToPremium,
        onRemoveAppClick = { app ->
            appPendingRemoval = app
        },
        onAddAppClick = {
            if (uiState.isQuotaReached) {
                toastState.show(
                    "Free plan limit reached (${uiState.currentAppsCount}/${uiState.maxFreeAppsAllowed}). Upgrade to add more!",
                    ToastType.INFO
                )
                onNavigateToPremium()
            } else {
                showAppPickerDialog = true
            }
        },
        modifier = modifier
    )

    // Remove Confirmation Pop-up Dialog
    appPendingRemoval?.let { app ->
        AlertDialog(
            onDismissRequest = { appPendingRemoval = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Remove ${app.appName}?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${app.appName} from your protected apps list?",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pkg = app.packageName
                        val name = app.appName
                        appPendingRemoval = null
                        viewModel.onRemoveAppClicked(pkg)
                        toastState.show("Removed $name from protected apps", ToastType.WARNING)
                        if (!com.app.privacyscreendisplay.core.ads.AdConfig.isPremiumUser) {
                            com.app.privacyscreendisplay.core.ads.InterstitialAdManager.showAdWithLoading(
                                context = context,
                                onLoadingStateChanged = { isAdLoading = it },
                                onAdDismissed = {}
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { appPendingRemoval = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    AppPickerDialog(
        isVisible = showAppPickerDialog,
        onDismiss = { showAppPickerDialog = false },
        alreadyAddedPackages = uiState.protectedApps.map { it.packageName },
        onAppSelected = { app ->
            viewModel.onAddAppClicked(app)
            toastState.show("Added ${app.appName} to protected apps", ToastType.SUCCESS)
            if (!com.app.privacyscreendisplay.core.ads.AdConfig.isPremiumUser) {
                com.app.privacyscreendisplay.core.ads.InterstitialAdManager.showAdWithLoading(
                    context = context,
                    onLoadingStateChanged = { isAdLoading = it },
                    onAdDismissed = {}
                )
            }
        }
    )

    // Ad Loading Overlay (3-dot blue triangle loader without white card background)
    com.app.privacyscreendisplay.core.ui.components.AdLoadingOverlay(
        isVisible = isAdLoading
    )
}

@Composable
fun ProtectedAppsContent(
    uiState: ProtectedAppsUiState,
    onNavigateBack: () -> Unit,
    onUpgradeClick: () -> Unit,
    onRemoveAppClick: (ProtectedApp) -> Unit,
    onAddAppClick: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Top Row with Back Arrow and Title on the SAME LINE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Protected Apps",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            // 1. Hero Free Plan Quota Card (Suppressed for Premium Users)
            if (!uiState.isPremiumUser) {
                PlanQuotaCard(
                    currentCount = uiState.currentAppsCount,
                    maxAllowed = uiState.maxFreeAppsAllowed,
                    onUpgradeClick = onUpgradeClick
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Ads placement (Same as in Home Screen) placed right after Free plan card
            if (!uiState.isPremiumUser) {
                AdBannerCard(
                    onAdClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. Added Apps Section Header (Hides max free quota for Premium Users)
            val addedAppsHeaderText = if (uiState.isPremiumUser) {
                "Added Apps (${uiState.currentAppsCount})"
            } else {
                "Added Apps (${uiState.currentAppsCount}/${uiState.maxFreeAppsAllowed})"
            }

            Text(
                text = addedAppsHeaderText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            AddedAppItemCard(
                apps = uiState.protectedApps,
                onRemoveClick = onRemoveAppClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Add App Action Button (Moved UP directly below Added Apps list)
            AddAppButton(onClick = onAddAppClick)

            // 5. Upgrade to Premium Prompt Card (Suppressed for Premium Users)
            if (!uiState.isPremiumUser) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Want to protect more apps?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                UpgradePremiumPromptCard(onUpgradeClick = onUpgradeClick)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
