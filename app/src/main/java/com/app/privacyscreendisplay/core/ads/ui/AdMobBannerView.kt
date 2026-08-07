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
 * Jetpack Compose wrapper rendering AdMob + Meta Mediation Live Banner Ads.
 * Uses a fixed 60.dp height container to prevent UI layout shifts/shaking.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier
) {
    if (AdConfig.isPremiumUser) return

    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }
    var adViewInstance by remember { mutableStateOf<AdView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            adViewInstance?.destroy()
            adViewInstance = null
        }
    }

    // Reserved fixed 60.dp height container to eliminate UI layout shifts/shaking
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdLoaded && !isAdFailed) {
            AdSkeletonShimmer(modifier = Modifier.fillMaxSize())
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(getAdaptiveAdSize(ctx))
                    setAdUnitId(AdConfig.BANNER_AD_UNIT_ID)
                    onPaidEventListener = AdRevenueTracker("Banner", AdConfig.BANNER_AD_UNIT_ID) { responseInfo }
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                            isAdFailed = false
                            AdLogger.i("AdMobBannerView", "SUCCESS: Banner Ad loaded successfully!")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isAdLoaded = false
                            isAdFailed = true
                            val reason = when (error.code) {
                                0 -> "Internal Error (0)"
                                1 -> "Invalid Request (1)"
                                2 -> "Network Error (2)"
                                3 -> "No Fill (3)"
                                else -> "Error Code ${error.code}"
                            }
                            AdLogger.e("AdMobBannerView", "FAILURE: Banner Ad failed to load! Reason: $reason | Msg: ${error.message}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
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
