package com.app.privacyscreendisplay.premium.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.privacyscreendisplay.core.ads.AdConfig
import com.app.privacyscreendisplay.core.ads.RewardedAdManager
import com.app.privacyscreendisplay.core.ui.components.LocalToastState
import com.app.privacyscreendisplay.core.ui.components.ToastType
import com.app.privacyscreendisplay.home.data.datasource.PrivacyGuardLocalDataSource
import kotlinx.coroutines.launch

/**
 * Pixel-perfect Upgrade to Premium Paywall Screen matching user design.
 */
@Composable
fun PremiumPaywallScreen(
    onNavigateBack: () -> Unit,
    onPremiumActivated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val toastState = LocalToastState.current
    val coroutineScope = rememberCoroutineScope()
    val localDataSource = remember(context) { PrivacyGuardLocalDataSource(context) }

    var isAdLoading by remember { mutableStateOf(false) }

    // Preload Rewarded Ad on Screen Launch
    LaunchedEffect(Unit) {
        RewardedAdManager.loadAd(context)
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Top Navigation Bar with Back Arrow
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
                        tint = Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Crown Illustration with Sparkles
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CrownHeroIllustration(modifier = Modifier.size(80.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // Title: Upgrade to Premium
                Text(
                    text = "Upgrade to Premium",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = "Unlock unlimited protection and\nan ad-free experience.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Feature Checklist Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                val features = listOf(
                    "Add unlimited apps",
                    "Remove all ads",
                    "Priority AI detection",
                    "Premium support",
                    "Access to premium themes"
                )

                features.forEachIndexed { index, feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = feature,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    if (index < features.size - 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Try Premium for 24 Hours Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Try Premium for 24 Hours",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Watch an ad to enjoy all premium features for 24 hours.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Watch Ad Button
                Button(
                    onClick = {
                        RewardedAdManager.showAdWithLoading(
                            context = context,
                            onLoadingStateChanged = { isAdLoading = it },
                            onUserEarnedReward = {
                                coroutineScope.launch {
                                    localDataSource.grant24HourPremium()
                                    toastState.show("🎉 24-Hour Premium Activated! All features unlocked.", ToastType.SUCCESS)
                                    onPremiumActivated()
                                    onNavigateBack()
                                }
                            },
                            onAdDismissedOrFailed = {}
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF047857)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.OndemandVideo,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Watch Ad",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Divider with "or" label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE2E8F0),
                    thickness = 1.dp
                )

                Text(
                    text = "or",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE2E8F0),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Go Premium Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        localDataSource.setPremiumStatus(true)
                        toastState.show("Upgraded to Premium successfully!", ToastType.SUCCESS)
                        onPremiumActivated()
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF047857)
                )
            ) {
                Text(
                    text = "Go Premium",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Pricing Text below button
            Text(
                text = "₹499.00 / year",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Ad Loading Overlay (3-dot blue triangle loader without white card background)
        com.app.privacyscreendisplay.core.ui.components.AdLoadingOverlay(
            isVisible = isAdLoading
        )
    }
}

/**
 * Custom vector illustration for the golden crown with diamond center and sparkle stars.
 */
@Composable
private fun CrownHeroIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val goldenBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFCD34D),
                    Color(0xFFF59E0B),
                    Color(0xFFD97706)
                )
            )

            // Sparkle 1 (Top Left)
            drawCircle(Color(0xFFFBBF24), radius = w * 0.04f, center = Offset(w * 0.18f, h * 0.28f))
            drawCircle(Color(0xFFFBBF24), radius = w * 0.025f, center = Offset(w * 0.10f, h * 0.15f))

            // Sparkle 2 (Top Right)
            drawCircle(Color(0xFFFBBF24), radius = w * 0.04f, center = Offset(w * 0.82f, h * 0.28f))
            drawCircle(Color(0xFFFBBF24), radius = w * 0.025f, center = Offset(w * 0.90f, h * 0.15f))

            // 3-Peak Crown Body
            val crownPath = Path().apply {
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

            // Crown Peak Circles (Golden Orbs)
            drawCircle(Color(0xFFFEF08A), radius = w * 0.055f, center = Offset(w * 0.15f, h * 0.35f))
            drawCircle(Color(0xFFFEF08A), radius = w * 0.065f, center = Offset(w * 0.50f, h * 0.22f))
            drawCircle(Color(0xFFFEF08A), radius = w * 0.055f, center = Offset(w * 0.85f, h * 0.35f))

            // Center Gem (Orange Oval)
            drawCircle(Color(0xFFEA580C), radius = w * 0.065f, center = Offset(w * 0.50f, h * 0.58f))
        }
    }
}
