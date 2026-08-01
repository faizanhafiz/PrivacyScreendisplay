package com.app.privacyscreendisplay.core.ads

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.app.privacyscreendisplay.core.ui.components.AdSkeletonShimmer
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

/**
 * Jetpack Compose AndroidView wrapper displaying real AdMob + Meta Mediation Banner Ads.
 * Integrates Wave Skeleton Shimmer loading state and AdMob compliant auto-refresh timer.
 *
 * @param adUnitId AdMob Ad Unit ID.
 * @param adSize Standard AdSize (defaults to [AdSize.BANNER]).
 * @param autoRefreshIntervalSeconds Refresh timer in seconds adhering to AdMob policy (defaults to 45s).
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.bannerAdUnitId,
    adSize: AdSize = AdSize.BANNER,
    autoRefreshIntervalSeconds: Long = 45L
) {
    var isLoading by remember { mutableStateOf(true) }
    var adViewRef by remember { mutableStateOf<AdView?>(null) }

    // Auto-Refresh Coroutine adhering to AdMob 30-60s refresh policy
    LaunchedEffect(adViewRef) {
        adViewRef?.let { view ->
            while (true) {
                delay(autoRefreshIntervalSeconds * 1000L)
                view.loadAd(AdManager.buildAdRequest())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            adViewRef?.destroy()
            adViewRef = null
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Wave Shimmer Skeleton UI while ad is loading
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AdSkeletonShimmer()
        }

        // Live AdMob / Meta Banner View
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(adSize)
                    setAdUnitId(adUnitId)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isLoading = false
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            isLoading = false
                        }
                    }
                    adViewRef = this
                    loadAd(AdManager.buildAdRequest())
                }
            },
            update = { adView ->
                // Banner view handles dynamic updates
            }
        )
    }
}
