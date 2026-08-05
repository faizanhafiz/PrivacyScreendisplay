package com.app.privacyscreendisplay.core.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Production Ad Manager orchestrating AdMob Interstitial Ads for action triggers
 * (Clear Log, Delete Logs, Add/Remove Protected App) with center-screen loading & 4s timeout fallback.
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
        showAdWithLoading(
            context = context,
            onLoadingStateChanged = {},
            onAdDismissed = onAdDismissed
        )
    }

    /**
     * Shows Interstitial Ad with center-screen loading indicator callback.
     * If ad fails to load or takes >4 seconds, automatically grants what the user expected to get!
     */
    fun showAdWithLoading(
        context: Context,
        onLoadingStateChanged: (Boolean) -> Unit,
        onAdDismissed: () -> Unit
    ) {
        if (AdConfig.isPremiumUser) {
            onAdDismissed()
            return
        }

        val activity = context.findActivity()
        val existingAd = interstitialAd

        if (existingAd != null && activity != null) {
            showAdInternal(activity, context, existingAd, onAdDismissed)
            interstitialAd = null
        } else {
            // Show center-screen loading indicator while fetching ad
            onLoadingStateChanged(true)
            val adRequest = AdRequest.Builder().build()

            var isHandled = false
            fun finishAndProceed(loadedAd: InterstitialAd?) {
                if (isHandled) return
                isHandled = true
                onLoadingStateChanged(false)
                if (loadedAd != null && activity != null) {
                    showAdInternal(activity, context, loadedAd, onAdDismissed)
                } else {
                    // Ad failed or timed out -> Grant request success & expected action!
                    Log.w(TAG, "Interstitial ad load failed or timed out (4s). Automatically granting expected action.")
                    onAdDismissed()
                }
            }

            // 4-second timeout limit
            val handler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                Log.w(TAG, "Interstitial ad 4s timeout reached. Granting request success.")
                finishAndProceed(null)
            }
            handler.postDelayed(timeoutRunnable, 4000L)

            InterstitialAd.load(
                context,
                AdConfig.interstitialAdUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        handler.removeCallbacks(timeoutRunnable)
                        finishAndProceed(ad)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "Interstitial ad failed to load: ${adError.message}")
                        handler.removeCallbacks(timeoutRunnable)
                        finishAndProceed(null)
                    }
                }
            )
        }
    }

    private fun showAdInternal(
        activity: Activity,
        context: Context,
        ad: InterstitialAd,
        onAdDismissed: () -> Unit
    ) {
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
