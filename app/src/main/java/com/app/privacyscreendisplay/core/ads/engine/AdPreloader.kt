package com.app.privacyscreendisplay.core.ads.engine

import android.content.Context
import com.app.privacyscreendisplay.core.ads.appopen.AppOpenAdManager
import com.app.privacyscreendisplay.core.ads.banner.BannerAdManager
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.app.privacyscreendisplay.core.ads.interstitial.InterstitialAdManager
import com.app.privacyscreendisplay.core.ads.nativead.NativeAdCacheManager
import com.app.privacyscreendisplay.core.ads.rewarded.RewardedAdManager

/**
 * Orchestrates background preloading of all ad formats immediately after SDK initialization.
 */
object AdPreloader {

    private const val TAG = "AdPreloader"

    fun startPreloading(context: Context, appOpenAdManager: AppOpenAdManager? = null) {
        if (AdConfig.isPremiumUser) {
            AdLogger.d(TAG, "User is Premium. Preloading bypassed.")
            return
        }

        AdLogger.i(TAG, "Initiating global ad preloader...")
        appOpenAdManager?.fetchAd(showOnLoad = false)
        NativeAdCacheManager.preloadNativeAd(context)
        BannerAdManager.getOrCreateAdView(context)
        InterstitialAdManager.preload(context)
        RewardedAdManager.preload(context)
    }
}
