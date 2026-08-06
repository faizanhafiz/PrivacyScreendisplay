package com.app.privacyscreendisplay.core.ads

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
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
 * Centralized App-Wide Banner Ad Manager.
 * Maintains a single shared AdView instance across all screens to avoid policy violations
 * caused by frequent ad re-fetching during screen navigation.
 * Automatically auto-refreshes every 45 seconds at a safe, policy-compliant interval.
 */
object CentralizedBannerAdManager {

    private const val TAG = "CentralizedBannerAd"
    private const val REFRESH_INTERVAL_MS = 45_000L // 45 seconds policy compliant interval
    private const val RETRY_INTERVAL_MS = 15_000L // 15 seconds retry on error

    private var sharedAdView: AdView? = null

    private val _isAdLoadedState = MutableStateFlow(false)
    val isAdLoadedState: StateFlow<Boolean> = _isAdLoadedState.asStateFlow()

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
            setupSharedAdView()
            startAutoRefreshLoop()
        }
    }

    private fun createAdListener(): AdListener {
        return object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                _isAdLoadedState.value = true
                _isLoadingState.value = false
                Log.i(TAG, "SUCCESS: Centralized Banner Ad loaded successfully! [UnitID=${AdConfig.bannerAdUnitId}]")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                _isAdLoadedState.value = false
                _isLoadingState.value = false
                val reason = when (error.code) {
                    0 -> "Internal Error (0)"
                    1 -> "Invalid Request / Ad Unit ID (1)"
                    2 -> "Network Error (2)"
                    3 -> "No Fill / Ad Inventory Unavailable (3)"
                    else -> "Error Code ${error.code}"
                }
                Log.e(TAG, "FAILURE: Banner Ad failed to load! Reason: $reason | Message: ${error.message} | Domain: ${error.domain} [UnitID=${AdConfig.bannerAdUnitId}]")
            }

            override fun onAdOpened() {
                super.onAdOpened()
                Log.d(TAG, "Banner Ad clicked/opened by user.")
            }
        }
    }

    private fun setupSharedAdView() {
        val ctx = appContext ?: return
        if (sharedAdView == null) {
            Log.i(TAG, "Initializing Shared AdView (UnitID=${AdConfig.bannerAdUnitId}, TestMode=${AdConfig.IS_TEST_MODE})")
            sharedAdView = AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(AdConfig.bannerAdUnitId)
                adListener = createAdListener()
                Log.d(TAG, "Loading initial Banner Ad request...")
                loadAd(AdManager.buildAdRequest())
            }
        }
    }

    private fun startAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val isLoaded = _isAdLoadedState.value
                val delayMs = if (isLoaded) REFRESH_INTERVAL_MS else RETRY_INTERVAL_MS
                delay(delayMs)
                refreshAd()
            }
        }
    }

    private fun refreshAd() {
        if (AdConfig.isPremiumUser) return
        sharedAdView?.let { view ->
            val status = if (_isAdLoadedState.value) "Periodic 45s refresh" else "15s error retry"
            Log.d(TAG, "Executing Banner Ad fetch ($status)...")
            view.loadAd(AdManager.buildAdRequest())
        }
    }

    fun getOrCreateAdView(context: Context): AdView {
        initialize(context)
        val view = sharedAdView ?: AdView(context.applicationContext).apply {
            setAdSize(AdSize.BANNER)
            setAdUnitId(AdConfig.bannerAdUnitId)
            adListener = createAdListener()
            Log.d(TAG, "Loading Banner Ad request in getOrCreateAdView...")
            loadAd(AdManager.buildAdRequest())
            sharedAdView = this
        }

        // Safely unparent before attaching to new screen container
        (view.parent as? ViewGroup)?.removeView(view)
        return view
    }

    fun destroy() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        sharedAdView?.destroy()
        sharedAdView = null
        isInitialized = false
    }
}
