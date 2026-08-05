package com.app.privacyscreendisplay.core.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.privacyscreendisplay.core.ui.components.AdSkeletonShimmer

/**
 * Jetpack Compose wrapper rendering Google AdMob + Meta Mediation Live Banner Ads.
 * Uses CentralizedBannerAdManager to share a single AdView across all screens,
 * preventing policy violations by avoiding ad re-requests during screen navigation.
 * Auto-refreshes every 45 seconds at a safe, policy-compliant interval.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier
) {
    if (AdConfig.isPremiumUser) return

    val context = LocalContext.current
    val isLoading by CentralizedBannerAdManager.isLoadingState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            AdSkeletonShimmer(modifier = Modifier.fillMaxSize())
        }

        // Single Centralized AdMob Banner View shared across screens
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                CentralizedBannerAdManager.getOrCreateAdView(ctx)
            },
            update = { view ->
                // Shared AdView is automatically updated by CentralizedBannerAdManager
            }
        )
    }
}
