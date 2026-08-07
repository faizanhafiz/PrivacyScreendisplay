package com.app.privacyscreendisplay.core.ads.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.app.privacyscreendisplay.core.ads.engine.AdLogger
import com.app.privacyscreendisplay.core.ads.engine.AdRevenueTracker
import com.app.privacyscreendisplay.core.ui.components.AdSkeletonShimmer
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Jetpack Compose wrapper rendering AdMob Live Banner Ads.
 * Displays wave skeleton continuous shimmer while ad is loading/retrying, and continuously
 * retries fetching banner ads in background every 15s on failure.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier
) {
    if (AdConfig.isPremiumUser) return

    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adViewInstance by remember { mutableStateOf<AdView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            adViewInstance?.destroy()
            adViewInstance = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        // Continuously show wave skeleton shimmer until banner ad is loaded
        if (!isAdLoaded) {
            AdSkeletonShimmer(modifier = Modifier.fillMaxSize())
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .graphicsLayer { alpha = if (isAdLoaded) 1f else 0f },
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(getAdaptiveAdSize(ctx))
                    setAdUnitId(AdConfig.BANNER_AD_UNIT_ID)
                    onPaidEventListener = AdRevenueTracker("Banner", AdConfig.BANNER_AD_UNIT_ID) { responseInfo }

                    fun loadBannerWithRetry() {
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                                AdLogger.i("AdMobBannerView", "SUCCESS: Banner Ad loaded! [UnitID=${AdConfig.BANNER_AD_UNIT_ID}]")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isAdLoaded = false
                                val reason = when (error.code) {
                                    0 -> "Internal Error (0)"
                                    1 -> "Invalid Request (1)"
                                    2 -> "Network Error (2)"
                                    3 -> "No Fill (3)"
                                    else -> "Error Code ${error.code}"
                                }
                                AdLogger.e("AdMobBannerView", "FAILURE: Banner Ad failed to load! Reason: $reason | Msg: ${error.message}")

                                // Continuously retry loading banner ad after 15s delay while wave skeleton remains visible
                                postDelayed({
                                    if (!isAdLoaded) {
                                        AdLogger.i("AdMobBannerView", "Continuously retrying Banner Ad load after 15s delay...")
                                        loadBannerWithRetry()
                                    }
                                }, 15_000L)
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }

                    loadBannerWithRetry()
                    adViewInstance = this
                }
            }
        )
    }
}

private fun getAdaptiveAdSize(context: Context): AdSize {
    val displayMetrics = context.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels.toFloat()
    val density = displayMetrics.density
    val adWidth = (widthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}
