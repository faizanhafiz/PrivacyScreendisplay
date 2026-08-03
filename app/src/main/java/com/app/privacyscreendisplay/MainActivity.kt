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
import com.app.privacyscreendisplay.premium.presentation.ui.PremiumPaywallScreen
import com.app.privacyscreendisplay.setup.presentation.ui.PermissionCameraScreen
import com.app.privacyscreendisplay.setup.presentation.ui.PermissionOverlayScreen
import com.app.privacyscreendisplay.setup.presentation.ui.PermissionSetupIntroScreen
import com.app.privacyscreendisplay.setup.presentation.ui.PermissionUsageAccessScreen
import com.app.privacyscreendisplay.ui.theme.PrivacyScreendisplayTheme

class MainActivity : ComponentActivity() {

    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var protectedAppsViewModel: ProtectedAppsViewModel
    private var appOpenAdManager: AppOpenAdManager? = null
    private var pendingAutoEnableProtection = false

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

        // Home DI & ViewModel
        val homeDS = HomeModule.providePrivacyGuardLocalDataSource(applicationContext)
        val homeRepo = HomeModule.providePrivacyGuardRepository(homeDS)
        val getProtectionStatusUseCase = HomeModule.provideGetProtectionStatusUseCase(homeRepo)

        // Protected Apps DI & ViewModel
        val protectedAppsDS = ProtectedAppsModule.provideProtectedAppsLocalDataSource(applicationContext)
        val protectedAppsRepo = ProtectedAppsModule.provideProtectedAppsRepository(protectedAppsDS)
        protectedAppsViewModel = ProtectedAppsViewModel(
            getProtectedAppsUseCase = ProtectedAppsModule.provideGetProtectedAppsUseCase(protectedAppsRepo),
            addProtectedAppUseCase = ProtectedAppsModule.provideAddProtectedAppUseCase(protectedAppsRepo),
            removeProtectedAppUseCase = ProtectedAppsModule.provideRemoveProtectedAppUseCase(protectedAppsRepo),
            getProtectionStatusUseCase = getProtectionStatusUseCase
        )

        homeViewModel = HomeViewModel(
            getProtectionStatusUseCase = getProtectionStatusUseCase,
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
                                val startDest = when {
                                    isOnboardingCompleted != true -> "onboarding"
                                    getNextMissingPermissionRoute() != null -> "permission_setup_intro"
                                    else -> "home"
                                }

                                NavHost(
                                    navController = navController,
                                    startDestination = startDest
                                ) {
                                    composable("onboarding") {
                                        OnboardingScreen(
                                            viewModel = onboardingViewModel,
                                            onNavigateToHome = {
                                                navController.navigate("permission_setup_intro") {
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

                                    composable("permission_setup_intro") {
                                        PermissionSetupIntroScreen(
                                            onStartSetupClick = {
                                                val nextRoute = getNextMissingPermissionRoute() ?: "home"
                                                navController.navigate(nextRoute)
                                            }
                                        )
                                    }

                                    composable("permission_camera") {
                                        PermissionCameraScreen(
                                            onAllowCameraClick = {
                                                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this@MainActivity)
                                                val hasUsageAccess = ForegroundAppMonitor(this@MainActivity).hasUsageAccessPermission()

                                                val nextRoute = if (!hasOverlay) "permission_overlay"
                                                else if (!hasUsageAccess) "permission_usage_access"
                                                else null

                                                if (nextRoute != null) {
                                                    navController.navigate(nextRoute)
                                                } else {
                                                    navController.navigate("home") {
                                                        popUpTo("permission_setup_intro") { inclusive = true }
                                                    }
                                                }
                                            },
                                            onNotNowClick = {
                                                val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this@MainActivity)
                                                val hasUsageAccess = ForegroundAppMonitor(this@MainActivity).hasUsageAccessPermission()

                                                val nextRoute = if (!hasOverlay) "permission_overlay"
                                                else if (!hasUsageAccess) "permission_usage_access"
                                                else null

                                                if (nextRoute != null) {
                                                    navController.navigate(nextRoute)
                                                } else {
                                                    navController.navigate("home") {
                                                        popUpTo("permission_setup_intro") { inclusive = true }
                                                    }
                                                }
                                            },
                                            onNavigateBack = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }

                                    composable("permission_overlay") {
                                        PermissionOverlayScreen(
                                            onOpenSettingsClick = {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
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
                                                val hasUsageAccess = ForegroundAppMonitor(this@MainActivity).hasUsageAccessPermission()
                                                if (!hasUsageAccess) {
                                                    navController.navigate("permission_usage_access")
                                                } else {
                                                    navController.navigate("home") {
                                                        popUpTo("permission_setup_intro") { inclusive = true }
                                                    }
                                                }
                                            },
                                            onNotNowClick = {
                                                val hasUsageAccess = ForegroundAppMonitor(this@MainActivity).hasUsageAccessPermission()
                                                if (!hasUsageAccess) {
                                                    navController.navigate("permission_usage_access")
                                                } else {
                                                    navController.navigate("home") {
                                                        popUpTo("permission_setup_intro") { inclusive = true }
                                                    }
                                                }
                                            },
                                            onNavigateBack = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }

                                    composable("permission_usage_access") {
                                        PermissionUsageAccessScreen(
                                            onOpenUsageAccessClick = {
                                                val appMonitor = ForegroundAppMonitor(this@MainActivity)
                                                if (!appMonitor.hasUsageAccessPermission()) {
                                                    try {
                                                        appMonitor.openUsageAccessSettings()
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                navController.navigate("home") {
                                                    popUpTo("permission_setup_intro") { inclusive = true }
                                                }
                                            },
                                            onNotNowClick = {
                                                navController.navigate("home") {
                                                    popUpTo("permission_setup_intro") { inclusive = true }
                                                }
                                            },
                                            onNavigateBack = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }

                                    composable("home") {
                                        LaunchedEffect(Unit) {
                                            val hasCamera = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                            val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this@MainActivity)
                                            val hasUsageAccess = ForegroundAppMonitor(this@MainActivity).hasUsageAccessPermission()
                                            val allGranted = hasCamera && hasOverlay && hasUsageAccess

                                            if (allGranted) {
                                                com.app.privacyscreendisplay.core.service.PrivacyGuardService.startService(applicationContext)
                                                if (pendingAutoEnableProtection) {
                                                    pendingAutoEnableProtection = false
                                                    homeViewModel.enableProtection()
                                                }
                                            }

                                            (application as? PrivacyGuardApplication)?.appOpenAdManager?.let { manager ->
                                                appOpenAdManager = manager
                                                manager.isAllowedToShowAd = true
                                                manager.showAdIfAvailable(this@MainActivity)
                                            }
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
                                                if (navController.currentDestination?.route != "premium_paywall") {
                                                    navController.navigate("premium_paywall") {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            },
                                            onNavigateToPermission = { route ->
                                                pendingAutoEnableProtection = true
                                                navController.navigate(route)
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
                                                if (navController.currentDestination?.route != "premium_paywall") {
                                                    navController.navigate("premium_paywall") {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    composable("premium_paywall") {
                                        PremiumPaywallScreen(
                                            onNavigateBack = {
                                                navController.popBackStack()
                                            },
                                            onPremiumActivated = {
                                                // Premium activated state updated
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

    private fun getNextMissingPermissionRoute(): String? {
        val hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!hasCamera) return "permission_camera"

        val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
        if (!hasOverlay) return "permission_overlay"

        val hasUsageAccess = ForegroundAppMonitor(this).hasUsageAccessPermission()
        if (!hasUsageAccess) return "permission_usage_access"

        return null
    }
}