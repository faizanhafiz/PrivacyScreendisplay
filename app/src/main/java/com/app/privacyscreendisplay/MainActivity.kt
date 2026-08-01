package com.app.privacyscreendisplay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.privacyscreendisplay.core.ads.AppOpenAdManager
import com.app.privacyscreendisplay.core.monitor.ForegroundAppMonitor
import com.app.privacyscreendisplay.core.ui.components.LocalToastState
import com.app.privacyscreendisplay.core.ui.components.ModernToastHost
import com.app.privacyscreendisplay.core.ui.components.ToastType
import com.app.privacyscreendisplay.core.ui.components.rememberModernToastState
import com.app.privacyscreendisplay.home.di.HomeModule
import com.app.privacyscreendisplay.home.presentation.ui.HomeScreen
import com.app.privacyscreendisplay.home.presentation.viewmodel.HomeViewModel
import com.app.privacyscreendisplay.onboarding.di.OnboardingModule
import com.app.privacyscreendisplay.onboarding.presentation.ui.OnboardingScreen
import com.app.privacyscreendisplay.onboarding.presentation.viewmodel.OnboardingViewModel
import com.app.privacyscreendisplay.protectedapps.di.ProtectedAppsModule
import com.app.privacyscreendisplay.protectedapps.presentation.ui.ProtectedAppsScreen
import com.app.privacyscreendisplay.protectedapps.presentation.viewmodel.ProtectedAppsViewModel
import com.app.privacyscreendisplay.ui.theme.PrivacyScreendisplayTheme

class MainActivity : ComponentActivity() {

    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var protectedAppsViewModel: ProtectedAppsViewModel
    private lateinit var appOpenAdManager: AppOpenAdManager

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            com.app.privacyscreendisplay.core.service.PrivacyGuardService.startService(applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Onboarding DI & ViewModel
        val onboardingDS = OnboardingModule.provideOnboardingLocalDataSource(applicationContext)
        val onboardingRepo = OnboardingModule.provideOnboardingRepository(onboardingDS)
        onboardingViewModel = OnboardingViewModel(
            getOnboardingFeaturesUseCase = OnboardingModule.provideGetOnboardingFeaturesUseCase(onboardingRepo),
            completeOnboardingUseCase = OnboardingModule.provideCompleteOnboardingUseCase(onboardingRepo)
        )

        // Protected Apps DI & ViewModel
        val protectedAppsDS = ProtectedAppsModule.provideProtectedAppsLocalDataSource(applicationContext)
        val protectedAppsRepo = ProtectedAppsModule.provideProtectedAppsRepository(protectedAppsDS)
        protectedAppsViewModel = ProtectedAppsViewModel(
            getProtectedAppsUseCase = ProtectedAppsModule.provideGetProtectedAppsUseCase(protectedAppsRepo),
            addProtectedAppUseCase = ProtectedAppsModule.provideAddProtectedAppUseCase(protectedAppsRepo),
            removeProtectedAppUseCase = ProtectedAppsModule.provideRemoveProtectedAppUseCase(protectedAppsRepo)
        )

        // Home DI & ViewModel
        val homeDS = HomeModule.providePrivacyGuardLocalDataSource(applicationContext)
        val homeRepo = HomeModule.providePrivacyGuardRepository(homeDS)
        homeViewModel = HomeViewModel(
            getProtectionStatusUseCase = HomeModule.provideGetProtectionStatusUseCase(homeRepo),
            getProtectedAppsUseCase = ProtectedAppsModule.provideGetProtectedAppsUseCase(protectedAppsRepo),
            toggleProtectionUseCase = HomeModule.provideToggleProtectionUseCase(homeRepo),
            updateOverlayStyleUseCase = HomeModule.provideUpdateOverlayStyleUseCase(homeRepo),
            updateSensitivityUseCase = HomeModule.provideUpdateSensitivityUseCase(homeRepo)
        )

        setContent {
            PrivacyScreendisplayTheme {
                val toastState = rememberModernToastState()

                CompositionLocalProvider(LocalToastState provides toastState) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                            val isOnboardingCompleted by onboardingDS.isOnboardingCompletedFlow.collectAsState(initial = null)

                            if (isOnboardingCompleted != null) {
                                val startDest = if (isOnboardingCompleted == true) "home" else "onboarding"

                                NavHost(
                                    navController = navController,
                                    startDestination = startDest
                                ) {
                                    composable("onboarding") {
                                        OnboardingScreen(
                                            viewModel = onboardingViewModel,
                                            onNavigateToHome = {
                                                navController.navigate("home") {
                                                    popUpTo("onboarding") { inclusive = true }
                                                }
                                            },
                                            onNavigateToLearnMore = {
                                                toastState.show(
                                                    "Privacy Guard AI protects your screen from shoulder surfers on-device.",
                                                    ToastType.INFO
                                                )
                                            }
                                        )
                                    }

                                    composable("home") {
                                        // Execute permission asking & ad sequence ONLY when Home Screen mounts
                                        LaunchedEffect(Unit) {
                                            triggerHomeScreenPermissionsAndAds()
                                        }

                                        HomeScreen(
                                            viewModel = homeViewModel,
                                            onNavigateToSettings = {
                                                toastState.show("Opening Settings...", ToastType.INFO)
                                            },
                                            onNavigateToProtectedApps = {
                                                if (navController.currentDestination?.route != "protected_apps") {
                                                    navController.navigate("protected_apps") {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            },
                                            onNavigateToActivityLog = {
                                                toastState.show("Opening Activity Log...", ToastType.INFO)
                                            },
                                            onNavigateToPremium = {
                                                toastState.show("Opening Google Play Premium Billing...", ToastType.INFO)
                                            }
                                        )
                                    }

                                    composable("protected_apps") {
                                        ProtectedAppsScreen(
                                            viewModel = protectedAppsViewModel,
                                            onNavigateBack = {
                                                navController.popBackStack()
                                            },
                                            onNavigateToPremium = {
                                                toastState.show("Opening Premium Subscription Paywall...", ToastType.INFO)
                                            }
                                        )
                                    }
                                }
                            }

                            // App-wide animated toast banner host overlay
                            ModernToastHost(toastState = toastState)
                        }
                    }
                }
            }
        }
    }

    /**
     * Executes required permissions requests followed by the App Open Ad sequence.
     * Guaranteed to run ONLY when the user reaches the Home Screen (after Onboarding).
     */
    private fun triggerHomeScreenPermissionsAndAds() {
        checkAndRequestRequiredPermissions()

        (application as? PrivacyGuardApplication)?.appOpenAdManager?.let { manager ->
            appOpenAdManager = manager
            manager.showAdIfAvailable(this@MainActivity)
        }
    }

    private fun checkAndRequestRequiredPermissions() {
        // 1. Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            com.app.privacyscreendisplay.core.service.PrivacyGuardService.startService(applicationContext)
        }

        // 2. System Overlay Window Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Usage Access Permission for Foreground App Scoping
        val appMonitor = ForegroundAppMonitor(this)
        if (!appMonitor.hasUsageAccessPermission()) {
            try {
                appMonitor.openUsageAccessSettings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}