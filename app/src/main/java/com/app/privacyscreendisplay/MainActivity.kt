package com.app.privacyscreendisplay

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.privacyscreendisplay.home.di.HomeModule
import com.app.privacyscreendisplay.home.presentation.ui.HomeScreen
import com.app.privacyscreendisplay.home.presentation.viewmodel.HomeViewModel
import com.app.privacyscreendisplay.onboarding.di.OnboardingModule
import com.app.privacyscreendisplay.onboarding.presentation.ui.OnboardingScreen
import com.app.privacyscreendisplay.onboarding.presentation.viewmodel.OnboardingViewModel
import com.app.privacyscreendisplay.ui.theme.PrivacyScreendisplayTheme

import com.app.privacyscreendisplay.core.ads.AdManager
import com.app.privacyscreendisplay.core.ads.AppOpenAdManager

import com.app.privacyscreendisplay.protectedapps.di.ProtectedAppsModule
import com.app.privacyscreendisplay.protectedapps.presentation.ui.ProtectedAppsScreen
import com.app.privacyscreendisplay.protectedapps.presentation.viewmodel.ProtectedAppsViewModel

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import com.app.privacyscreendisplay.core.ui.components.LocalToastState
import com.app.privacyscreendisplay.core.ui.components.ModernToastHost
import com.app.privacyscreendisplay.core.ui.components.ToastType
import com.app.privacyscreendisplay.core.ui.components.rememberModernToastState

class MainActivity : ComponentActivity() {

    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var protectedAppsViewModel: ProtectedAppsViewModel
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access Application-level AppOpenAdManager
        (application as? PrivacyGuardApplication)?.appOpenAdManager?.let { manager ->
            appOpenAdManager = manager
            manager.showAdIfAvailable(this@MainActivity)
        }

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
        homeViewModel = HomeViewModel(
            getProtectionStatusUseCase = HomeModule.provideGetProtectionStatusUseCase(homeRepo),
            toggleProtectionUseCase = HomeModule.provideToggleProtectionUseCase(homeRepo),
            updateOverlayStyleUseCase = HomeModule.provideUpdateOverlayStyleUseCase(homeRepo),
            updateSensitivityUseCase = HomeModule.provideUpdateSensitivityUseCase(homeRepo)
        )

        // Protected Apps DI & ViewModel
        val protectedAppsDS = ProtectedAppsModule.provideProtectedAppsLocalDataSource(applicationContext)
        val protectedAppsRepo = ProtectedAppsModule.provideProtectedAppsRepository(protectedAppsDS)
        protectedAppsViewModel = ProtectedAppsViewModel(
            getProtectedAppsUseCase = ProtectedAppsModule.provideGetProtectedAppsUseCase(protectedAppsRepo),
            addProtectedAppUseCase = ProtectedAppsModule.provideAddProtectedAppUseCase(protectedAppsRepo),
            removeProtectedAppUseCase = ProtectedAppsModule.provideRemoveProtectedAppUseCase(protectedAppsRepo)
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

                            NavHost(
                                navController = navController,
                                startDestination = "onboarding"
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

                            // App-wide animated toast banner host overlay
                            ModernToastHost(toastState = toastState)
                        }
                    }
                }
            }
        }
    }
}