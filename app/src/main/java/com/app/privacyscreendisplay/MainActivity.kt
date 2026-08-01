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

class MainActivity : ComponentActivity() {

    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var homeViewModel: HomeViewModel

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
        homeViewModel = HomeViewModel(
            getProtectionStatusUseCase = HomeModule.provideGetProtectionStatusUseCase(homeRepo),
            toggleProtectionUseCase = HomeModule.provideToggleProtectionUseCase(homeRepo),
            updateOverlayStyleUseCase = HomeModule.provideUpdateOverlayStyleUseCase(homeRepo),
            updateSensitivityUseCase = HomeModule.provideUpdateSensitivityUseCase(homeRepo)
        )

        setContent {
            PrivacyScreendisplayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Privacy Guard AI protects your screen from shoulder surfers on-device.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToSettings = {
                                    Toast.makeText(this@MainActivity, "Opening Settings...", Toast.LENGTH_SHORT).show()
                                },
                                onNavigateToProtectedApps = {
                                    Toast.makeText(this@MainActivity, "Opening Protected Apps Selection...", Toast.LENGTH_SHORT).show()
                                },
                                onNavigateToActivityLog = {
                                    Toast.makeText(this@MainActivity, "Opening Activity Log...", Toast.LENGTH_SHORT).show()
                                },
                                onNavigateToPremium = {
                                    Toast.makeText(this@MainActivity, "Opening Google Play Premium Billing...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}