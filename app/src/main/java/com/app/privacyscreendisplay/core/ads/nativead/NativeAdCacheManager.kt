package com.app.privacyscreendisplay.core.ads.nativead

import android.content.Context
import com.app.privacyscreendisplay.core.ads.config.AdConfig
import com.app.privacyscreendisplay.core.ads.engine.AdLogger
import com.app.privacyscreendisplay.core.ads.engine.AdNetworkMonitor
import com.app.privacyscreendisplay.core.ads.engine.AdRetryPolicy
import com.app.privacyscreendisplay.core.ads.engine.AdRevenueTracker
import com.app.privacyscreendisplay.core.ads.model.AdState
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.VideoOptions
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
 * Centralized Native Ad Cache Manager.
 * Maintains pre-loaded shared NativeAd instances, handles auto-refreshes (40s),
 * exponential retries, and clean native ad destruction.
 */
object NativeAdCacheManager {

    private const val TAG = "NativeAdCacheManager"
    private const val REFRESH_INTERVAL_MS = 40_000L

    private val mutex = Mutex()
    private val retryPolicy = AdRetryPolicy()

    private val _nativeAdState = MutableStateFlow<NativeAd?>(null)
    val nativeAdState: StateFlow<NativeAd?> = _nativeAdState.asStateFlow()

    private val _adState = MutableStateFlow(AdState.IDLE)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val _isLoadingState = MutableStateFlow(false)
    val isLoadingState: StateFlow<Boolean> = _isLoadingState.asStateFlow()

    private var autoRefreshJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastContext: Context? = null

    fun preloadNativeAd(context: Context) {
        if (AdConfig.isPremiumUser) return
        lastContext = context
        scope.launch {
            mutex.withLock {
                fetchAdInternal(context)
            }
        }
    }

    private fun fetchAdInternal(context: Context) {
        if (AdConfig.isPremiumUser || _isLoadingState.value) return
        if (!AdNetworkMonitor.isNetworkAvailable.value) {
            AdLogger.w(TAG, "Network offline. Native Ad fetch postponed.")
            _adState.value = AdState.RETRYING
            return
        }

        _isLoadingState.value = true
        _adState.value = AdState.LOADING

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val nativeAdOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        val adLoader = AdLoader.Builder(context, AdConfig.NATIVE_AD_UNIT_ID)
            .withNativeAdOptions(nativeAdOptions)
            .forNativeAd { newlyLoadedAd ->
                newlyLoadedAd.setOnPaidEventListener(
                    AdRevenueTracker("Native", AdConfig.NATIVE_AD_UNIT_ID) { newlyLoadedAd.responseInfo }
                )
                val previousAd = _nativeAdState.value
                _nativeAdState.value = newlyLoadedAd
                _isLoadingState.value = false
                _adState.value = AdState.LOADED
                retryPolicy.reset()
                previousAd?.destroy()

                AdLogger.i(TAG, "SUCCESS: Centralized Native Ad loaded.")
                scheduleAutoRefresh(context)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    _isLoadingState.value = false
                    _adState.value = AdState.RETRYING

                    val reason = when (error.code) {
                        0 -> "Internal Error (0)"
                        1 -> "Invalid Request (1)"
                        2 -> "Network Error (2)"
                        3 -> "No Fill (3)"
                        else -> "Error Code ${error.code}"
                    }
                    AdLogger.e(TAG, "FAILURE: Native Ad failed to load! Reason: $reason | Msg: ${error.message}")

                    val nextDelay = retryPolicy.getNextDelayMs()
                    AdLogger.i(TAG, "Scheduling Native Ad retry in ${nextDelay / 1000}s (Attempt #${retryPolicy.getRetryCount()})")

                    autoRefreshJob?.cancel()
                    autoRefreshJob = scope.launch {
                        delay(nextDelay)
                        preloadNativeAd(context)
                    }
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun scheduleAutoRefresh(context: Context) {
        autoRefreshJob?.cancel()
        autoRefreshJob = scope.launch {
            delay(REFRESH_INTERVAL_MS)
            AdLogger.i(TAG, "Auto-refreshing Native Ad after 40s interval...")
            preloadNativeAd(context)
        }
    }

    fun destroy() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        _nativeAdState.value?.destroy()
        _nativeAdState.value = null
        _isLoadingState.value = false
        _adState.value = AdState.DESTROYED
    }
}
