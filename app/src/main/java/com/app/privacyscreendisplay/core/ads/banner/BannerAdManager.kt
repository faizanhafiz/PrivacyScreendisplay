package com.app.privacyscreendisplay.core.ads.banner

import android.content.Context
import android.view.ViewGroup
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.app.privacyscreendisplay.core.ads.engine.AdLogger
import com.app.privacyscreendisplay.core.ads.engine.AdNetworkMonitor
import com.app.privacyscreendisplay.core.ads.engine.AdRetryPolicy
import com.app.privacyscreendisplay.core.ads.engine.AdRevenueTracker
import com.app.privacyscreendisplay.core.ads.model.AdState
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Centralized Banner Ad Manager.
 * Maintains a single shared AdView instance across screens to avoid policy violations.
 * Auto-refreshes every 45s and retries with exponential backoff on failure.
 */
object BannerAdManager {

    private const val TAG = "BannerAdManager"
    private const val REFRESH_INTERVAL_MS = 45_000L

    private var sharedAdView: AdView? = null
    private val retryPolicy = AdRetryPolicy()

    private val _adState = MutableStateFlow(AdState.IDLE)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val _isAdLoadedState = MutableStateFlow(false)
    val isAdLoadedState: StateFlow<Boolean> = _isAdLoadedState.asStateFlow()

    private val _isLoadingState = MutableStateFlow(false)
    val isLoadingState: StateFlow<Boolean> = _isLoadingState.asStateFlow()

    private var refreshJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun getOrCreateAdView(context: Context): AdView {
        if (AdConfig.isPremiumUser) {
            sharedAdView?.destroy()
            sharedAdView = null
            _isAdLoadedState.value = false
            return AdView(context)
        }

        val existingView = sharedAdView
        val view = if (existingView != null) {
            existingView
        } else {
            val newView = AdView(context).apply {
                setAdSize(getAdaptiveAdSize(context))
                setAdUnitId(AdConfig.BANNER_AD_UNIT_ID)
                onPaidEventListener = AdRevenueTracker("Banner", AdConfig.BANNER_AD_UNIT_ID) { responseInfo }
                adListener = createAdListener(context)
            }
            sharedAdView = newView
            loadBannerInternal(newView)
            newView
        }

        (view.parent as? ViewGroup)?.removeView(view)
        return view
    }

    fun loadBanner(context: Context) {
        if (AdConfig.isPremiumUser) return
        val view = getOrCreateAdView(context)
        if (!_isAdLoadedState.value && !_isLoadingState.value) {
            loadBannerInternal(view)
        }
    }

    private fun loadBannerInternal(adView: AdView) {
        if (AdConfig.isPremiumUser || _isLoadingState.value) return
        if (!AdNetworkMonitor.isNetworkAvailable.value) {
            AdLogger.w(TAG, "Network offline. Banner load postponed.")
            _adState.value = AdState.RETRYING
            return
        }

        _isLoadingState.value = true
        _adState.value = AdState.LOADING
        AdLogger.i(TAG, "Requesting Banner Ad (UnitID=${AdConfig.BANNER_AD_UNIT_ID})...")
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun createAdListener(context: Context): AdListener {
        return object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                _isAdLoadedState.value = true
                _isLoadingState.value = false
                _adState.value = AdState.LOADED
                retryPolicy.reset()
                AdLogger.i(TAG, "SUCCESS: Banner Ad loaded.")
                scheduleAutoRefresh(context)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                _isAdLoadedState.value = false
                _isLoadingState.value = false
                _adState.value = AdState.RETRYING

                val reason = parseLoadErrorCode(error.code)
                AdLogger.e(TAG, "FAILURE: Banner Ad failed to load! Reason: $reason | Msg: ${error.message}")

                val nextDelay = retryPolicy.getNextDelayMs()
                AdLogger.i(TAG, "Scheduling Banner retry in ${nextDelay / 1000}s (Attempt #${retryPolicy.getRetryCount()})")

                refreshJob?.cancel()
                refreshJob = scope.launch {
                    delay(nextDelay)
                    sharedAdView?.let { loadBannerInternal(it) }
                }
            }
        }
    }

    private fun scheduleAutoRefresh(context: Context) {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            delay(REFRESH_INTERVAL_MS)
            AdLogger.i(TAG, "Auto-refreshing Banner Ad after 45s interval...")
            sharedAdView?.let { loadBannerInternal(it) }
        }
    }

    private fun getAdaptiveAdSize(context: Context): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels.toFloat()
        val density = displayMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
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
        refreshJob?.cancel()
        refreshJob = null
        sharedAdView?.destroy()
        sharedAdView = null
        _isAdLoadedState.value = false
        _isLoadingState.value = false
        _adState.value = AdState.DESTROYED
    }
}
