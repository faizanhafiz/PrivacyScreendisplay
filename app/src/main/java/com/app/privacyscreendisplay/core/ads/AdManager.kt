package com.app.privacyscreendisplay.core.ads

import android.content.Context
import com.app.privacyscreendisplay.BuildConfig
import com.app.privacyscreendisplay.core.ads.appopen.AppOpenAdManager
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.app.privacyscreendisplay.core.ads.engine.AdLogger
import com.app.privacyscreendisplay.core.ads.engine.AdNetworkMonitor
import com.app.privacyscreendisplay.core.ads.engine.AdPreloader
import com.app.privacyscreendisplay.core.ads.interstitial.InterstitialAdManager
import com.app.privacyscreendisplay.core.ads.nativead.NativeAdCacheManager
import com.app.privacyscreendisplay.core.ads.rewarded.RewardedAdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.flow.StateFlow

/**
 * Main Singleton AdManager Facade.
 * Initializes MobileAds SDK, configures Ad Inspector in Debug builds, launches network monitoring,
 * and exposes unified ad operations to the application.
 */
object AdManager {

    private const val TAG = "AdManager"
    private var isInitialized = false

    fun initialize(
        context: Context,
        appOpenAdManager: AppOpenAdManager? = null,
        onInitializationComplete: () -> Unit = {}
    ) {
        if (isInitialized) {
            onInitializationComplete()
            return
        }

        AdLogger.i(TAG, "Initializing Google Mobile Ads SDK (AppID=${AdConfig.APP_ID}, Debug=${BuildConfig.DEBUG})...")

        // Start offline/online network state observer
        AdNetworkMonitor.startMonitoring(context) {
            AdLogger.i(TAG, "Network restored. Triggering ad preloader refill...")
            AdPreloader.startPreloading(context, appOpenAdManager)
        }

        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapterClass, status) in statusMap) {
                AdLogger.d(TAG, "Adapter: $adapterClass, State: ${status.initializationState}, Desc: ${status.description}")
            }
            isInitialized = true
            AdPreloader.startPreloading(context, appOpenAdManager)
            onInitializationComplete()
        }
    }

    /**
     * Opens Google Ad Inspector in Debug builds only.
     */
    fun openAdInspector(context: Context) {
        if (BuildConfig.DEBUG) {
            MobileAds.openAdInspector(context) { error ->
                if (error != null) {
                    AdLogger.e(TAG, "Ad Inspector error: ${error.message}")
                } else {
                    AdLogger.i(TAG, "Ad Inspector opened successfully.")
                }
            }
        }
    }

    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
