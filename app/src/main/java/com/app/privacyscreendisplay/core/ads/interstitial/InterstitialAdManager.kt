package com.app.privacyscreendisplay.core.ads.interstitial

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
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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

/**
 * Production Centralized Interstitial Ad Manager.
 * Features: Background preloading, immediate refill after show, 4-second loading rule with
 * background continuation, late-fill caching, exponential backoff retries, and thread safety.
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"
    private const val FOUR_SECONDS_TIMEOUT_MS = 4000L

    private val mutex = Mutex()
    private val retryPolicy = AdRetryPolicy()
    private val scope = CoroutineScope(Dispatchers.Main)

    @Volatile
    private var cachedAd: InterstitialAd? = null

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

    private fun fetchInternal(context: Context, onLoadedCallback: ((InterstitialAd) -> Unit)? = null) {
        if (AdConfig.isPremiumUser || _isLoadingState.value || cachedAd != null) return
        if (!AdNetworkMonitor.isNetworkAvailable.value) {
            AdLogger.w(TAG, "Network offline. Interstitial fetch postponed.")
            _adState.value = AdState.RETRYING
            return
        }

        val activity = context.findActivity() ?: context
        _isLoadingState.value = true
        _adState.value = AdState.LOADING
        AdLogger.i(TAG, "Fetching Interstitial Ad (UnitID=${AdConfig.INTERSTITIAL_AD_UNIT_ID})...")

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            AdConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.onPaidEventListener = AdRevenueTracker("Interstitial", AdConfig.INTERSTITIAL_AD_UNIT_ID) { ad.responseInfo }
                    cachedAd = ad
                    _isLoadingState.value = false
                    _adState.value = AdState.LOADED
                    retryPolicy.reset()
                    AdLogger.i(TAG, "SUCCESS: Interstitial Ad loaded & cached.")

                    onLoadedCallback?.invoke(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    cachedAd = null
                    _isLoadingState.value = false
                    _adState.value = AdState.RETRYING

                    val reason = parseLoadErrorCode(error.code)
                    AdLogger.e(TAG, "FAILURE: Interstitial Ad failed to load! Reason: $reason | Msg: ${error.message}")

                    val nextDelay = retryPolicy.getNextDelayMs()
                    AdLogger.i(TAG, "Scheduling Interstitial retry in ${nextDelay / 1000}s (Attempt #${retryPolicy.getRetryCount()})")

                    retryJob?.cancel()
                    retryJob = scope.launch {
                        delay(nextDelay)
                        preload(context)
                    }
                }
            }
        )
    }

    fun showAdWithTimeout(
        context: Context,
        onLoadingStateChanged: (Boolean) -> Unit = {},
        onAdDismissed: () -> Unit
    ) {
        if (AdConfig.isPremiumUser) {
            onAdDismissed()
            return
        }

        val activity = context.findActivity()
        val ad = cachedAd

        if (ad != null && activity != null) {
            AdLogger.i(TAG, "Displaying pre-cached Interstitial Ad instantly.")
            showAdInternal(activity, context, ad, onAdDismissed)
            cachedAd = null
            preload(context) // Immediately refill next ad
        } else {
            // Initiate 4-second loading rule with background continuation
            onLoadingStateChanged(true)
            var isHandled = false
            val handler = Handler(Looper.getMainLooper())

            fun finishAndProceed(loadedAd: InterstitialAd?) {
                if (isHandled) return
                isHandled = true
                onLoadingStateChanged(false)

                if (loadedAd != null && activity != null && !activity.isFinishing && !activity.isDestroyed) {
                    AdLogger.i(TAG, "Ad loaded within 4s window. Presenting Interstitial.")
                    showAdInternal(activity, context, loadedAd, onAdDismissed)
                    cachedAd = null
                    preload(context)
                } else {
                    AdLogger.w(TAG, "4-second timeout reached or load pending. Proceeding UI action; continuing background load.")
                    onAdDismissed()
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
        ad: InterstitialAd,
        onAdDismissed: () -> Unit
    ) {
        _adState.value = AdState.SHOWING
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                _adState.value = AdState.DISMISSED
                cachedAd = null
                onAdDismissed()
                preload(context)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                _adState.value = AdState.DISMISSED
                AdLogger.e(TAG, "Interstitial Ad failed to show: ${adError.message}")
                cachedAd = null
                onAdDismissed()
                preload(context)
            }
        }
        ad.show(activity)
    }

    private fun Context.findActivity(): Activity? {
        var current = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
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

    fun destroy() {
        retryJob?.cancel()
        retryJob = null
        cachedAd = null
        _isLoadingState.value = false
        _adState.value = AdState.DESTROYED
    }
}
