package com.app.privacyscreendisplay.core.ads.appopen

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * App Open Ad Manager observing Application lifecycle & foregrounding transitions.
 * Features: Background pre-caching, 4-hour expiration validation, screen suppression rules,
 * exponential retries, and process recreation safety.
 */
class AppOpenAdManager(
    private val application: Application
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var isPendingShowOnLoad = false
    private var currentActivity: Activity? = null
    private var loadTime: Long = 0
    private var hasShownAdThisSession = false

    private val retryPolicy = AdRetryPolicy()
    private var retryJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _adState = MutableStateFlow(AdState.IDLE)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    /**
     * Controls whether App Open Ads are allowed to be shown.
     * Set to true ONLY when user reaches the Home screen (suppressed during Onboarding/Wizard).
     */
    var isAllowedToShowAd: Boolean = false

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun fetchAd(targetActivity: Activity? = null, showOnLoad: Boolean = false) {
        if (AdConfig.isPremiumUser) {
            AdLogger.d(TAG, "User is Premium subscriber. App Open Ad suppressed.")
            return
        }

        if (targetActivity != null) {
            currentActivity = targetActivity
        }

        if (showOnLoad) {
            isPendingShowOnLoad = true
        }

        if (isAdAvailable()) {
            AdLogger.d(TAG, "App Open Ad available. Presenting if pending...")
            if (isPendingShowOnLoad) {
                currentActivity?.let { showAdIfAvailable(it) }
            }
            return
        }

        if (isLoadingAd) {
            AdLogger.d(TAG, "App Open Ad is currently loading...")
            return
        }

        if (!AdNetworkMonitor.isNetworkAvailable.value) {
            AdLogger.w(TAG, "Network offline. App Open fetch postponed.")
            _adState.value = AdState.RETRYING
            return
        }

        isLoadingAd = true
        _adState.value = AdState.LOADING
        AdLogger.i(TAG, "Fetching App Open Ad (UnitID=${AdConfig.APP_OPEN_AD_UNIT_ID})...")

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            AdConfig.APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    ad.onPaidEventListener = AdRevenueTracker("AppOpen", AdConfig.APP_OPEN_AD_UNIT_ID) { ad.responseInfo }
                    appOpenAd = ad
                    isLoadingAd = false
                    _adState.value = AdState.LOADED
                    retryPolicy.reset()
                    loadTime = Date().time
                    AdLogger.i(TAG, "SUCCESS: App Open Ad loaded.")

                    if (isPendingShowOnLoad) {
                        currentActivity?.let { activity ->
                            showAdIfAvailable(activity)
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    isPendingShowOnLoad = false
                    _adState.value = AdState.RETRYING

                    val reason = parseLoadErrorCode(loadAdError.code)
                    AdLogger.e(TAG, "FAILURE: App Open Ad failed to load! Reason: $reason | Msg: ${loadAdError.message}")

                    val nextDelay = retryPolicy.getNextDelayMs()
                    AdLogger.i(TAG, "Scheduling App Open retry in ${nextDelay / 1000}s (Attempt #${retryPolicy.getRetryCount()})")

                    retryJob?.cancel()
                    retryJob = scope.launch {
                        delay(nextDelay)
                        fetchAd(targetActivity, showOnLoad)
                    }
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    fun showAdIfAvailable(activity: Activity) {
        if (AdConfig.isPremiumUser || !isAllowedToShowAd || hasShownAdThisSession || isShowingAd) {
            return
        }

        currentActivity = activity
        isPendingShowOnLoad = true

        if (!isAdAvailable()) {
            AdLogger.d(TAG, "App Open Ad not available yet. Fetching now to display on load...")
            fetchAd(targetActivity = activity, showOnLoad = true)
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        _adState.value = AdState.SHOWING
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                _adState.value = AdState.DISMISSED
                appOpenAd = null
                isShowingAd = false
                isPendingShowOnLoad = false
                AdLogger.d(TAG, "App Open Ad dismissed. Pre-fetching next ad & refreshing banner...")
                currentActivity?.let { com.app.privacyscreendisplay.core.ads.banner.BannerAdManager.loadBanner(it) }
                fetchAd(showOnLoad = false)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                _adState.value = AdState.DISMISSED
                appOpenAd = null
                isShowingAd = false
                isPendingShowOnLoad = false
                AdLogger.e(TAG, "App Open Ad failed to show: ${adError.message}")
                currentActivity?.let { com.app.privacyscreendisplay.core.ads.banner.BannerAdManager.loadBanner(it) }
                fetchAd(showOnLoad = false)
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                isPendingShowOnLoad = false
                hasShownAdThisSession = true
                AdLogger.d(TAG, "App Open Ad showing full screen.")
            }
        }

        isShowingAd = true
        isPendingShowOnLoad = false
        hasShownAdThisSession = true
        appOpenAd?.show(activity)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        hasShownAdThisSession = false
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (AdConfig.isPremiumUser || !isAllowedToShowAd) return
        currentActivity?.let { activity ->
            showAdIfAvailable(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
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

    companion object {
        private const val TAG = "AppOpenAdManager"
    }
}
