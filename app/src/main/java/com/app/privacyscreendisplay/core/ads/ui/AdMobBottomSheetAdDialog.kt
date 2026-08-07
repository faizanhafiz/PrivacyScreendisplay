package com.app.privacyscreendisplay.core.ads.ui

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.privacyscreendisplay.core.ads.AdManager
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * High-Converting Interactive Bottom-Sheet Modal Ad Dialog for Home Screen.
 * Uses independent Activity-scoped AdLoader for maximum fill reliability.
 */
@Composable
fun AdMobBottomSheetAdDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    adUnitId: String = AdConfig.NATIVE_AD_UNIT_ID,
    onAdClick: () -> Unit = {}
) {
    if (!isVisible || AdConfig.isPremiumUser) return

    val context = LocalContext.current
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var isAdLoadedSuccessfully by remember { mutableStateOf(false) }

    DisposableEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { loadedAd ->
                nativeAdState = loadedAd
                isAdLoadedSuccessfully = true
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.e("AdMobBottomSheetAd", "Bottom Sheet Native Ad failed to load: code=${error.code}, msg=${error.message}")
                    isAdLoadedSuccessfully = false
                    onDismiss()
                }
            })
            .build()

        adLoader.loadAd(AdManager.buildAdRequest())

        onDispose {
            nativeAdState?.destroy()
            nativeAdState = null
        }
    }

    val ad = nativeAdState
    if (!isAdLoadedSuccessfully || ad == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible && isAdLoadedSuccessfully,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFFF1E4DE))
                    .navigationBarsPadding()
                    .clickable(enabled = false) {}
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFCBD5E1))
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "SPONSORED PROMOTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.2.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.06f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Ad",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { ctx ->
                            val adView = NativeAdView(ctx)
                            val container = LinearLayout(ctx).apply {
                                orientation = LinearLayout.VERTICAL
                                gravity = Gravity.CENTER_HORIZONTAL
                            }

                            val headlineView = TextView(ctx).apply {
                                textSize = 18f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(android.graphics.Color.parseColor("#1E293B"))
                                gravity = Gravity.CENTER
                                text = ad.headline
                            }

                            val bodyView = TextView(ctx).apply {
                                textSize = 12f
                                setTextColor(android.graphics.Color.parseColor("#475569"))
                                gravity = Gravity.CENTER
                                setPadding(0, 6, 0, 14)
                                text = ad.body ?: "Exclusive feature offer"
                            }

                            val density = ctx.resources.displayMetrics.density
                            val mediaHeightPx = (200 * density).toInt()

                            val mediaView = MediaView(ctx).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    mediaHeightPx
                                ).apply {
                                    setMargins(0, 0, 0, 16)
                                }
                            }

                            val ctaHeightPx = (48 * density).toInt()
                            val ctaButton = Button(ctx).apply {
                                textSize = 14f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(android.graphics.Color.WHITE)
                                setBackgroundColor(android.graphics.Color.parseColor("#FF5500"))
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ctaHeightPx
                                )
                                text = (ad.callToAction ?: "INSTALL").uppercase()
                            }

                            container.addView(headlineView)
                            container.addView(bodyView)
                            container.addView(mediaView)
                            container.addView(ctaButton)

                            adView.addView(container)

                            adView.headlineView = headlineView
                            adView.bodyView = bodyView
                            adView.mediaView = mediaView
                            adView.callToActionView = ctaButton

                            adView.setNativeAd(ad)
                            adView
                        }
                    )
                }
            }
        }
    }
}
