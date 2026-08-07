package com.app.privacyscreendisplay.core.ads.rewarded

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.app.privacyscreendisplay.core.ads.engine.AdLogger
import com.app.privacyscreendisplay.core.ads.engine.AdNetworkMonitor
import com.app.privacyscreendisplay.core.ads.engine.AdRetryPolicy
import com.app.privacyscreendisplay.core.ads.engine.AdRevenueTracker
import com.app.privacyscreendisplay.core.ads.model.AdState
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Preloaded Rewarded Ad Manager.
 * Enforces single-reward guarantee and 4-second loading timeout rule:
 * - If loaded within 4 seconds: shows ad and grants reward upon completion.
 * - If NOT loaded within 4 seconds: dismisses loading indicator, grants reward fallback,
 *   and continues loading in background without cancelling request.
 */
object RewardedAdManager {

    private const val TAG = "RewardedAdManager"
    private const val FOUR_SECONDS_TIMEOUT_MS = 4000L

    private val mutex = Mutex()
    private val retryPolicy = AdRetryPolicy()
    private val scope = CoroutineScope(Dispatchers.Main)

    @Volatile
    private var cachedAd: RewardedAd? = null

    private val _adState = MutableStateFlow(AdState.IDLE)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val _isLoadingState = MutableStateFlow(false)
    val isLoadingState: StateFlow<Boolean> = _isLoadingState.asStateFlow()

    private var retryJob: Job? = null

    fun preload(context: Context) {
        if (AdConfig.isPremiumUser) return
        scope.launch {
            mutex.withLock {
                fetchInternal(context)
            }
        }
    }

    private fun fetchInternal(context: Context, onLoadedCallback: ((RewardedAd) -> Unit)? = null) {
        if (AdConfig.isPremiumUser || _isLoadingState.value || cachedAd != null) return
        if (!AdNetworkMonitor.isNetworkAvailable.value) {
            AdLogger.w(TAG, "Network offline. Rewarded fetch postponed.")
            _adState.value = AdState.RETRYING
            return
        }

        val activity = context.findActivity() ?: context
        _isLoadingState.value = true
        _adState.value = AdState.LOADING
        AdLogger.i(TAG, "Fetching Rewarded Ad (UnitID=${AdConfig.REWARDED_AD_UNIT_ID})...")

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            AdConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    ad.onPaidEventListener = AdRevenueTracker("Rewarded", AdConfig.REWARDED_AD_UNIT_ID) { ad.responseInfo }
                    cachedAd = ad
                    _isLoadingState.value = false
                    _adState.value = AdState.LOADED
                    retryPolicy.reset()
                    AdLogger.i(TAG, "SUCCESS: Rewarded Ad loaded & cached.")

                    onLoadedCallback?.invoke(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    cachedAd = null
                    _isLoadingState.value = false
                    _adState.value = AdState.RETRYING

                    val reason = parseLoadErrorCode(error.code)
                    AdLogger.e(TAG, "FAILURE: Rewarded Ad failed to load! Reason: $reason | Msg: ${error.message}")

                    val nextDelay = retryPolicy.getNextDelayMs()
                    AdLogger.i(TAG, "Scheduling Rewarded retry in ${nextDelay / 1000}s (Attempt #${retryPolicy.getRetryCount()})")

                    retryJob?.cancel()
                    retryJob = scope.launch {
                        delay(nextDelay)
                        preload(activity)
                    }
                }
            }
        )
    }

    fun showAdWithTimeout(
        context: Context,
        onLoadingStateChanged: (Boolean) -> Unit = {},
        onUserEarnedReward: () -> Unit,
        onAdDismissedOrFailed: () -> Unit = {}
    ) {
        if (AdConfig.isPremiumUser) {
            onUserEarnedReward()
            onAdDismissedOrFailed()
            return
        }

        val activity = context.findActivity()
        val ad = cachedAd

        if (ad != null && activity != null) {
            AdLogger.i(TAG, "Displaying pre-cached Rewarded Ad instantly.")
            showAdInternal(activity, context, ad, onUserEarnedReward, onAdDismissedOrFailed)
            cachedAd = null
            preload(context)
        } else {
            onLoadingStateChanged(true)
            var isHandled = false
            val handler = Handler(Looper.getMainLooper())

            fun finishAndProceed(loadedAd: RewardedAd?) {
                if (isHandled) return
                isHandled = true
                onLoadingStateChanged(false)

                if (loadedAd != null && activity != null && !activity.isFinishing && !activity.isDestroyed) {
                    AdLogger.i(TAG, "Ad loaded within 4s window. Presenting Rewarded Ad.")
                    showAdInternal(activity, context, loadedAd, onUserEarnedReward, onAdDismissedOrFailed)
                    cachedAd = null
                    preload(context)
                } else {
                    AdLogger.w(TAG, "4-second timeout reached or load pending. Granting reward fallback; continuing background load.")
                    onUserEarnedReward()
                    onAdDismissedOrFailed()
                }
            }

            val timeoutRunnable = Runnable {
                finishAndProceed(null)
            }
            handler.postDelayed(timeoutRunnable, FOUR_SECONDS_TIMEOUT_MS)

            fetchInternal(context) { newlyLoadedAd ->
                handler.removeCallbacks(timeoutRunnable)
                finishAndProceed(newlyLoadedAd)
            }
        }
    }

    private fun showAdInternal(
        activity: Activity,
        context: Context,
        ad: RewardedAd,
        onUserEarnedReward: () -> Unit,
        onAdDismissedOrFailed: () -> Unit
    ) {
        val hasRewardedUser = AtomicBoolean(false)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                _adState.value = AdState.DISMISSED
                AdLogger.i(TAG, "Rewarded Ad dismissed by user.")
                onAdDismissedOrFailed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                _adState.value = AdState.DISMISSED
                AdLogger.e(TAG, "Rewarded Ad failed to show: ${error.message}")
                if (hasRewardedUser.compareAndSet(false, true)) {
                    onUserEarnedReward()
                }
                onAdDismissedOrFailed()
            }

            override fun onAdShowedFullScreenContent() {
                _adState.value = AdState.SHOWING
                AdLogger.i(TAG, "Rewarded Ad showing on screen.")
            }
        }

        ad.show(activity) { rewardItem ->
            AdLogger.i(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            if (hasRewardedUser.compareAndSet(false, true)) {
                onUserEarnedReward()
            }
        }
    }

    private fun parseLoadErrorCode(code: Int): String {
        return when (code) {
            0 -> "Internal Error (0)"
            1 -> "Invalid Request (1)"
            2 -> "Network Error (2)"
            3 -> "No Fill / Inventory Unavailable (3)"
            else -> "Error Code $code"
        }
    }

    fun isAdAvailable(): Boolean = cachedAd != null
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
