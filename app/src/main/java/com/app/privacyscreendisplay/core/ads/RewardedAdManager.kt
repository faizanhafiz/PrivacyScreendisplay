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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Production Ad Manager orchestrating AdMob Rewarded Video Ads for 24-Hour Premium Access
 * and Photo Unblur action rewards with center-screen loading & 4s timeout fallback.
 */
object RewardedAdManager {

    private const val TAG = "RewardedAdManager"
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun loadAd(context: Context) {
        if (isLoading || rewardedAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            AdConfig.rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
                }
            }
        )
    }

    fun showAd(
        context: Context,
        onUserEarnedReward: () -> Unit,
        onAdDismissedOrFailed: () -> Unit = {}
    ) {
        showAdWithLoading(
            context = context,
            onLoadingStateChanged = {},
            onUserEarnedReward = onUserEarnedReward,
            onAdDismissedOrFailed = onAdDismissedOrFailed
        )
    }

    /**
     * Shows Rewarded Ad with center-screen loading indicator callback.
     * If ad fails to load or takes >4 seconds, automatically grants what the user expected to get!
     */
    fun showAdWithLoading(
        context: Context,
        onLoadingStateChanged: (Boolean) -> Unit,
        onUserEarnedReward: () -> Unit,
        onAdDismissedOrFailed: () -> Unit = {}
    ) {
        if (AdConfig.isPremiumUser) {
            onUserEarnedReward()
            return
        }

        val activity = context.findActivity()
        val existingAd = rewardedAd

        if (existingAd != null && activity != null) {
            showAdInternal(activity, context, existingAd, onUserEarnedReward, onAdDismissedOrFailed)
            rewardedAd = null
        } else {
            // Show center-screen loading indicator while fetching ad
            onLoadingStateChanged(true)
            val adRequest = AdRequest.Builder().build()

            var isHandled = false
            fun finishAndProceed(loadedAd: RewardedAd?) {
                if (isHandled) return
                isHandled = true
                onLoadingStateChanged(false)
                if (loadedAd != null && activity != null) {
                    showAdInternal(activity, context, loadedAd, onUserEarnedReward, onAdDismissedOrFailed)
                } else {
                    // Ad failed or timed out -> Grant request success & expected reward!
                    Log.w(TAG, "Rewarded ad load failed or timed out (4s). Automatically granting expected reward.")
                    onUserEarnedReward()
                    onAdDismissedOrFailed()
                }
            }

            // 4-second timeout limit
            val handler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                Log.w(TAG, "Rewarded ad 4s timeout reached. Granting request success.")
                finishAndProceed(null)
            }
            handler.postDelayed(timeoutRunnable, 4000L)

            RewardedAd.load(
                context,
                AdConfig.rewardedAdUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        handler.removeCallbacks(timeoutRunnable)
                        finishAndProceed(ad)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
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
        ad: RewardedAd,
        onUserEarnedReward: () -> Unit,
        onAdDismissedOrFailed: () -> Unit
    ) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd(context)
                onAdDismissedOrFailed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                loadAd(context)
                // Grant reward if ad failed to show
                onUserEarnedReward()
                onAdDismissedOrFailed()
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onUserEarnedReward()
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
