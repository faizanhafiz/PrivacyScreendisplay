package com.app.privacyscreendisplay.core.ads

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * High-Converting Interactive Bottom-Sheet Modal Ad Dialog with precise pixel alignment,
 * navigation bar insets, and high-contrast orange action CTA button.
 */
@Composable
fun AdMobBottomSheetAdDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    adUnitId: String = AdConfig.nativeAdUnitId,
    onAdClick: () -> Unit = {}
) {
    if (!isVisible) return

    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            nativeAdState?.destroy()
            nativeAdState = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFFF1E4DE)) // Warm aesthetic dialog background matching screenshot
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
                    // Top Drag Handle & Close Button Header
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Drag handle bar
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFCBD5E1))
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Swipe down to close",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Top-Right Close 'X' Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF64748B).copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Ad",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live AdMob / Meta Native Advanced Layout
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            val adView = NativeAdView(context)
                            val container = LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                gravity = Gravity.CENTER_HORIZONTAL
                            }

                            // Headline Title View
                            val headlineView = TextView(context).apply {
                                textSize = 18f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(android.graphics.Color.parseColor("#1E293B"))
                                gravity = Gravity.CENTER
                            }

                            // Subtitle Body View
                            val bodyView = TextView(context).apply {
                                textSize = 12f
                                setTextColor(android.graphics.Color.parseColor("#475569"))
                                gravity = Gravity.CENTER
                                setPadding(0, 6, 0, 14)
                            }

                            // Central High-Res Graphic MediaView
                            val density = context.resources.displayMetrics.density
                            val mediaHeightPx = (200 * density).toInt()

                            val mediaView = MediaView(context).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    mediaHeightPx
                                ).apply {
                                    setMargins(0, 0, 0, 16)
                                }
                            }

                            // Full-Width High-Contrast Orange Action CTA Button ("INSTALL" / "LEARN MORE")
                            val ctaHeightPx = (48 * density).toInt()
                            val ctaButton = Button(context).apply {
                                textSize = 14f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(android.graphics.Color.WHITE)
                                setBackgroundColor(android.graphics.Color.parseColor("#FF5500"))
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ctaHeightPx
                                )
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

                            val adLoader = AdLoader.Builder(context, adUnitId)
                                .forNativeAd { loadedAd ->
                                    nativeAdState = loadedAd
                                    (adView.headlineView as? TextView)?.text = loadedAd.headline
                                    (adView.bodyView as? TextView)?.text = loadedAd.body ?: "Exclusive feature offer"
                                    (adView.callToActionView as? Button)?.text = (loadedAd.callToAction ?: "INSTALL").uppercase()

                                    adView.setNativeAd(loadedAd)
                                    isLoading = false
                                }
                                .withAdListener(object : AdListener() {
                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        super.onAdFailedToLoad(error)
                                        isLoading = false
                                    }
                                })
                                .build()

                            adLoader.loadAd(AdManager.buildAdRequest())
                            adView
                        }
                    )
                }
            }
        }
    }
}
