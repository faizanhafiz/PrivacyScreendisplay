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
import androidx.compose.ui.graphics.graphicsLayer
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
    val isAdLoaded by CentralizedBannerAdManager.isAdLoadedState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading || !isAdLoaded) {
            AdSkeletonShimmer(modifier = Modifier.fillMaxSize())
        }

        // Single Centralized AdMob Banner View shared across screens, centered in container
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = if (isAdLoaded) 1f else 0f },
            factory = { ctx ->
                android.widget.FrameLayout(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val adView = CentralizedBannerAdManager.getOrCreateAdView(ctx)
                    val lp = android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                    addView(adView, lp)
                }
            },
            update = { containerView ->
                val adView = CentralizedBannerAdManager.getOrCreateAdView(containerView.context)
                if (adView.parent != containerView) {
                    (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                    val lp = android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                    containerView.removeAllViews()
                    containerView.addView(adView, lp)
                }
            }
        )
    }
}
