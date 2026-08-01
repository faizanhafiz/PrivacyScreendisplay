package com.app.privacyscreendisplay.onboarding.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.privacyscreendisplay.onboarding.presentation.ui.components.OnboardingFeatureCard
import com.app.privacyscreendisplay.onboarding.presentation.ui.components.OnboardingHeroIllustration
import com.app.privacyscreendisplay.onboarding.presentation.viewmodel.OnboardingUiEvent
import com.app.privacyscreendisplay.onboarding.presentation.viewmodel.OnboardingUiState
import com.app.privacyscreendisplay.onboarding.presentation.viewmodel.OnboardingViewModel

/**
 * Stateful Onboarding Screen entry point observing ViewModel state & events.
 *
 * @param viewModel ViewModel orchestrating state for the screen.
 * @param onNavigateToHome Callback invoked upon completing onboarding.
 * @param onNavigateToLearnMore Callback invoked when user taps 'Learn more'.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLearnMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is OnboardingUiEvent.NavigateToHome -> onNavigateToHome()
                is OnboardingUiEvent.NavigateToLearnMore -> onNavigateToLearnMore()
            }
        }
    }

    OnboardingScreenContent(
        uiState = uiState,
        onGetStartedClick = viewModel::onGetStartedClicked,
        onLearnMoreClick = viewModel::onLearnMoreClicked,
        modifier = modifier
    )
}

/**
 * Stateless Jetpack Compose implementation of the Onboarding screen design.
 * Fully state-hoisted for reusability, previews, and testing.
 *
 * @param uiState Immutable state for rendering.
 * @param onGetStartedClick Action callback for primary button.
 * @param onLearnMoreClick Action callback for secondary link.
 */
@Composable
fun OnboardingScreenContent(
    uiState: OnboardingUiState,
    onGetStartedClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greenPrimaryColor = Color(0xFF16A34A)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator(color = greenPrimaryColor)
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Graphic Illustration
                    OnboardingHeroIllustration()

                    Spacer(modifier = Modifier.height(24.dp))

                    // App Title & Tagline
                    Text(
                        text = "AI Privacy Guard",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Stay private. Everywhere.",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subtitle Body Description
                    Text(
                        text = "Our AI detects if someone is looking at your screen and instantly protects your content with a secure blur overlay.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Feature Highlights Section
                    uiState.features.forEach { feature ->
                        OnboardingFeatureCard(item = feature)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Bottom Action Buttons Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Primary Green "Get Started" Pill Button
                    Button(
                        onClick = onGetStartedClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenPrimaryColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary "Learn more" Text Action
                    TextButton(
                        onClick = onLearnMoreClick
                    ) {
                        Text(
                            text = "Learn more",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = greenPrimaryColor
                        )
                    }
                }
            }
        }
    }
}
