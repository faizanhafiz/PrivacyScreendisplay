package com.app.privacyscreendisplay.core.ads

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.privacyscreendisplay.core.ui.components.AdSkeletonShimmer
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.delay

/**
 * Jetpack Compose wrapper rendering Google AdMob + Meta Mediation Live Native Ads.
 * Includes AdMob policy compliant auto-refresh (45s) and Shimmer loading wave fallback.
 */
@Composable
fun AdMobNativeAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.nativeAdUnitId,
    autoRefreshIntervalSeconds: Long = 45L,
    onAdLoaded: () -> Unit = {}
) {
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var adLoaderRef by remember { mutableStateOf<AdLoader?>(null) }

    // Auto-Refresh Coroutine adhering to AdMob 30-60s policy for Native Ads
    LaunchedEffect(adLoaderRef) {
        adLoaderRef?.let { loader ->
            while (true) {
                delay(autoRefreshIntervalSeconds * 1000L)
                loader.loadAd(AdManager.buildAdRequest())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAdState?.destroy()
            nativeAdState = null
            adLoaderRef = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AdSkeletonShimmer()
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                val adView = NativeAdView(context)
                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 32, 32, 32)
                }

                // Top Badge Header
                val headerRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val adBadge = TextView(context).apply {
                    text = "Ad"
                    textSize = 10f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(android.graphics.Color.parseColor("#D97706"))
                    setBackgroundColor(android.graphics.Color.parseColor("#FEF3C7"))
                    setPadding(12, 4, 12, 4)
                }



                headerRow.addView(adBadge)


                // Body Content Row
                val bodyRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 16, 0, 0)
                }

                val iconView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(96, 96)
                }

                val textColumnParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(16, 0, 16, 0)
                }

                val textColumn = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = textColumnParams
                }

                val headlineView = TextView(context).apply {
                    textSize = 13f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(android.graphics.Color.parseColor("#1E293B"))
                }

                val bodyView = TextView(context).apply {
                    textSize = 11f
                    setTextColor(android.graphics.Color.parseColor("#64748B"))
                }

                textColumn.addView(headlineView)
                textColumn.addView(bodyView)

                val ctaView = Button(context).apply {
                    textSize = 11f
                    setTextColor(android.graphics.Color.WHITE)
                    setBackgroundColor(android.graphics.Color.parseColor("#2563EB"))
                }

                bodyRow.addView(iconView)
                bodyRow.addView(textColumn)
                bodyRow.addView(ctaView)

                container.addView(headerRow)
                container.addView(bodyRow)

                adView.addView(container)

                adView.headlineView = headlineView
                adView.bodyView = bodyView
                adView.iconView = iconView
                adView.callToActionView = ctaView

                // Hide native view layout initially so default button/text doesn't bleed through shimmer wave
                adView.visibility = View.INVISIBLE

                val adLoader = AdLoader.Builder(context, adUnitId)
                    .forNativeAd { loadedAd ->
                        nativeAdState?.destroy()
                        nativeAdState = loadedAd
                        (adView.headlineView as? TextView)?.text = loadedAd.headline
                        (adView.bodyView as? TextView)?.text = loadedAd.body ?: ""
                        (adView.callToActionView as? Button)?.text = loadedAd.callToAction ?: "Install"

                        if (loadedAd.icon != null) {
                            (adView.iconView as? ImageView)?.setImageDrawable(loadedAd.icon?.drawable)
                            adView.iconView?.visibility = View.VISIBLE
                        } else {
                            adView.iconView?.visibility = View.GONE
                        }

                        adView.setNativeAd(loadedAd)
                        adView.visibility = View.VISIBLE
                        isLoading = false
                        onAdLoaded()
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            adView.visibility = View.GONE
                            isLoading = false
                        }
                    })
                    .build()

                adLoaderRef = adLoader
                adLoader.loadAd(AdManager.buildAdRequest())
                adView
            },
            update = {
                // Native ad view update
            }
        )
    }
}
