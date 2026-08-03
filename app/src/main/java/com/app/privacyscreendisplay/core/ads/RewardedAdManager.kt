package com.app.privacyscreendisplay.core.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Singleton Ad Manager orchestrating AdMob Rewarded Video Ads for 24-Hour Premium Access rewards.
 */
object RewardedAdManager {

    private const val TAG = "RewardedAdManager"
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun loadAd(context: Context, onAdLoaded: () -> Unit = {}, onAdFailedToLoad: (String) -> Unit = {}) {
        if (isLoading || rewardedAd != null) {
            if (rewardedAd != null) onAdLoaded()
            return
        }

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
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
                    onAdFailedToLoad(adError.message)
                }
            }
        )
    }

    fun showAd(
        context: Context,
        onUserEarnedReward: () -> Unit,
        onAdDismissedOrFailed: () -> Unit
    ) {
        val activity = context.findActivity()

        val ad = rewardedAd
        if (ad != null && activity != null) {
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward()
            }
            rewardedAd = null
            loadAd(context)
        } else {
            // Attempt fast load or grant reward directly so user experience is not blocked
            loadAd(
                context = context,
                onAdLoaded = {
                    showAd(context, onUserEarnedReward, onAdDismissedOrFailed)
                },
                onAdFailedToLoad = {
                    Log.w(TAG, "Ad load failed. Granting fallback reward for 24h Premium.")
                    onUserEarnedReward()
                    onAdDismissedOrFailed()
                }
            )
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
