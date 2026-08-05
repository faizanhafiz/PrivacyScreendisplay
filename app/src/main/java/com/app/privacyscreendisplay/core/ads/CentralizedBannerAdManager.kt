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

    private fun setupSharedAdView() {
        val ctx = appContext ?: return
        if (sharedAdView == null) {
            sharedAdView = AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(AdConfig.bannerAdUnitId)
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        _isAdLoadedState.value = true
                        _isLoadingState.value = false
                        Log.d(TAG, "Centralized Banner Ad loaded successfully.")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        _isLoadingState.value = false
                        Log.e(TAG, "Centralized Banner Ad failed to load (code=${error.code}): ${error.message}")
                    }
                }
                loadAd(AdManager.buildAdRequest())
            }
        }
    }

    private fun startAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                refreshAd()
            }
        }
    }

    private fun refreshAd() {
        if (AdConfig.isPremiumUser) return
        sharedAdView?.let { view ->
            Log.d(TAG, "Executing 45s policy-compliant background Banner Ad refresh.")
            view.loadAd(AdManager.buildAdRequest())
        }
    }

    fun getOrCreateAdView(context: Context): AdView {
        initialize(context)
        val view = sharedAdView ?: AdView(context.applicationContext).apply {
            setAdSize(AdSize.BANNER)
            setAdUnitId(AdConfig.bannerAdUnitId)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    _isAdLoadedState.value = true
                    _isLoadingState.value = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    _isLoadingState.value = false
                }
            }
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
