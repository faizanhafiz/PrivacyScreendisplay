package com.app.privacyscreendisplay.core.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Singleton Ad Manager orchestrating AdMob Interstitial Ads for action triggers
 * (Clear Log, Add/Remove Protected App).
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun loadAd(context: Context) {
        if (AdConfig.isPremiumUser) return
        if (isLoading || interstitialAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AdConfig.interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.e(TAG, "Interstitial ad failed to load: ${adError.message}")
                }
            }
        )
    }

    fun showAd(context: Context, onAdDismissed: () -> Unit) {
        if (AdConfig.isPremiumUser) {
            onAdDismissed()
            return
        }

        val activity = context.findActivity()
        val ad = interstitialAd

        if (ad != null && activity != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadAd(context)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadAd(context)
                    onAdDismissed()
                }
            }
            ad.show(activity)
            interstitialAd = null
        } else {
            // If ad is not ready, proceed without blocking user action
            loadAd(context)
            onAdDismissed()
        }
    }

    private fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }
}
