package com.app.privacyscreendisplay.core.ads.ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.app.privacyscreendisplay.core.ads.nativead.NativeAdCacheManager
import com.app.privacyscreendisplay.core.ui.components.AdSkeletonShimmer
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Jetpack Compose wrapper rendering AdMob + Meta Mediation Live Native Ads.
 * Uses NativeAdCacheManager to share pre-loaded native ads safely across screens.
 */
@Composable
fun AdMobNativeAdView(
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {}
) {
    if (AdConfig.isPremiumUser) return

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        NativeAdCacheManager.preloadNativeAd(context)
    }

    val currentAd by NativeAdCacheManager.nativeAdState.collectAsState()
    val isLoading by NativeAdCacheManager.isLoadingState.collectAsState()

    val ad = currentAd
    if (isLoading || ad == null) {
        AdSkeletonShimmer(modifier = modifier.height(72.dp))
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val density = ctx.resources.displayMetrics.density
                    val iconSizePx = (44 * density).toInt()
                    val ctaHeightPx = (34 * density).toInt()
                    val marginPx = (12 * density).toInt()
                    val paddingPx = (14 * density).toInt()

                    val adView = NativeAdView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val container = LinearLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                    }

                    val iconView = ImageView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx)
                    }

                    val minMediaPx = (120 * density).toInt()
                    val mediaView = MediaView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(minMediaPx, minMediaPx)
                        visibility = View.GONE
                    }

                    val textColumnParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        setMargins(marginPx, 0, marginPx, 0)
                    }

                    val textColumn = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = textColumnParams
                    }

                    val headlineView = TextView(ctx).apply {
                        textSize = 13f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(android.graphics.Color.parseColor("#1E293B"))
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }

                    val bodyView = TextView(ctx).apply {
                        textSize = 11f
                        setTextColor(android.graphics.Color.parseColor("#64748B"))
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }

                    textColumn.addView(headlineView)
                    textColumn.addView(bodyView)

                    val ctaView = Button(ctx).apply {
                        textSize = 11f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor(android.graphics.Color.parseColor("#2563EB"))
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ctaHeightPx
                        )
                    }

                    container.addView(iconView)
                    container.addView(textColumn)
                    container.addView(ctaView)
                    container.addView(mediaView)

                    adView.addView(container)

                    adView.headlineView = headlineView
                    adView.bodyView = bodyView
                    adView.iconView = iconView
                    adView.mediaView = mediaView
                    adView.callToActionView = ctaView

                    adView.visibility = View.INVISIBLE
                    adView
                },
                update = { adView ->
                    (adView.headlineView as? TextView)?.text = ad.headline
                    (adView.bodyView as? TextView)?.text = ad.body ?: ""
                    (adView.callToActionView as? Button)?.text = ad.callToAction ?: "Install"

                    if (ad.icon != null) {
                        (adView.iconView as? ImageView)?.setImageDrawable(ad.icon?.drawable)
                        adView.iconView?.visibility = View.VISIBLE
                    } else {
                        adView.iconView?.visibility = View.GONE
                    }

                    val hasMedia = ad.mediaContent != null && (ad.mediaContent?.hasVideoContent() == true || ad.mediaContent?.mainImage != null)
                    adView.mediaView?.visibility = if (hasMedia) View.VISIBLE else View.GONE

                    adView.setNativeAd(ad)
                    adView.visibility = View.VISIBLE
                    onAdLoaded()
                }
            )
        }
    }
}
