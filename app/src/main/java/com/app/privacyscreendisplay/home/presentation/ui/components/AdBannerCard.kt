package com.app.privacyscreendisplay.home.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.privacyscreendisplay.core.ads.AdConfig
import com.app.privacyscreendisplay.core.ads.AdMobBannerView

/**
 * Banner Ad Container component.
 * Directly renders Google AdMob + Meta Mediation Live Banner Ads (AdView).
 */
@Composable
fun AdBannerCard(
    modifier: Modifier = Modifier,
    onAdClick: () -> Unit = {}
) {
    if (AdConfig.isPremiumUser) return
    AdMobBannerView(modifier = modifier)
}
