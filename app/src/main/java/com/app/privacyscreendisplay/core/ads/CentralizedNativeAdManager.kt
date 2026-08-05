package com.app.privacyscreendisplay.core.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.VideoOptions

/**
 * Centralized App-Wide Native Ad Manager.
 * Maintains a single pre-loaded NativeAd instance shared across screens to avoid policy violations
 * caused by frequent ad re-fetching on screen navigation.
 * Automatically auto-refreshes every 40 seconds.
 */
object CentralizedNativeAdManager {

    private const val TAG = "CentralizedNativeAd"
    private const val REFRESH_INTERVAL_MS = 40_000L // 40 seconds
    private const val RETRY_INTERVAL_MS = 15_000L // 15 seconds retry on error

    private val _nativeAdState = MutableStateFlow<NativeAd?>(null)
    val nativeAdState: StateFlow<NativeAd?> = _nativeAdState.asStateFlow()

    private val _isLoadingState = MutableStateFlow(true)
    val isLoadingState: StateFlow<Boolean> = _isLoadingState.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var isInitialized = false
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (AdConfig.isPremiumUser) return
        appContext = context.applicationContext

        if (!isInitialized) {
            isInitialized = true
            startAutoRefreshLoop()
        }
    }

    private fun startAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val success = fetchAdInternal()
                val nextDelay = if (success) REFRESH_INTERVAL_MS else RETRY_INTERVAL_MS
                delay(nextDelay)
            }
        }
    }

    private fun fetchAdInternal(): Boolean {
        val ctx = appContext ?: return false
        if (AdConfig.isPremiumUser) return false

        var isFetchSuccess = true
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val nativeAdOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        val adLoader = AdLoader.Builder(ctx, AdConfig.nativeAdUnitId)
            .withNativeAdOptions(nativeAdOptions)
            .forNativeAd { newlyLoadedAd ->
                val previousAd = _nativeAdState.value
                _nativeAdState.value = newlyLoadedAd
                _isLoadingState.value = false
                previousAd?.destroy()
                Log.d(TAG, "Centralized Native Ad loaded successfully & updated app-wide.")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.e(TAG, "Centralized Native Ad failed to load (code=${error.code}): ${error.message}")
                    _isLoadingState.value = false
                    isFetchSuccess = false
                }
            })
            .build()

        adLoader.loadAd(AdManager.buildAdRequest())
        return isFetchSuccess
    }
}
