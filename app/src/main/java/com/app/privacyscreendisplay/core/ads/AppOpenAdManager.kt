package com.app.privacyscreendisplay.core.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * Google AdMob & Meta Mediation App Open Ad Manager.
 * Guarantees App Open Ads present EVERY SINGLE TIME the app is opened (cold start or foreground resume).
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

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Loads an App Open Ad.
     * @param targetActivity Optional target activity to display ad immediately on load.
     * @param showOnLoad If true, guarantees ad presentation as soon as loading completes.
     */
    fun fetchAd(targetActivity: Activity? = null, showOnLoad: Boolean = false) {
        if (AdConfig.isPremiumUser) {
            Log.d(TAG, "User is Premium subscriber. App Open Ad suppressed.")
            return
        }

        if (targetActivity != null) {
            currentActivity = targetActivity
        }

        if (showOnLoad) {
            isPendingShowOnLoad = true
        }

        if (isAdAvailable()) {
            Log.d(TAG, "App Open Ad is available. Presenting...")
            if (isPendingShowOnLoad) {
                currentActivity?.let { showAdIfAvailable(it) }
            }
            return
        }

        if (isLoadingAd) {
            Log.d(TAG, "App Open Ad is currently loading... Marked pending display.")
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            application,
            AdConfig.appOpenAdUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(TAG, "App Open Ad loaded successfully!")

                    // Guarantee display if user requested display on open
                    if (isPendingShowOnLoad) {
                        currentActivity?.let { activity ->
                            showAdIfAvailable(activity)
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    isPendingShowOnLoad = false
                    Log.e(TAG, "App Open Ad failed to load: code=${loadAdError.code}, msg=${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Checks if ad is available and hasn't expired (Google 4-hour policy rule).
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /**
     * Displays the App Open Ad if available, or queues it to display as soon as loading completes.
     */
    fun showAdIfAvailable(activity: Activity) {
        if (AdConfig.isPremiumUser) {
            Log.d(TAG, "User is Premium subscriber. App Open Ad suppressed.")
            return
        }

        currentActivity = activity
        isPendingShowOnLoad = true

        if (isShowingAd) {
            Log.d(TAG, "App Open Ad is already showing.")
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "App Open Ad not available yet. Fetching now to display on load...")
            fetchAd(targetActivity = activity, showOnLoad = true)
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.d(TAG, "Target activity is finishing or destroyed.")
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                isPendingShowOnLoad = false
                Log.d(TAG, "App Open Ad dismissed. Pre-fetching next ad...")
                fetchAd(showOnLoad = false)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                isPendingShowOnLoad = false
                Log.e(TAG, "App Open Ad failed to show: ${adError.message}")
                fetchAd(showOnLoad = false)
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                isPendingShowOnLoad = false
                Log.d(TAG, "App Open Ad showing full screen.")
            }
        }

        isShowingAd = true
        isPendingShowOnLoad = false
        appOpenAd?.show(activity)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (AdConfig.isPremiumUser) {
            Log.d(TAG, "User is Premium subscriber. OnStart App Open Ad suppressed.")
            return
        }

        Log.d(TAG, "App transitioned to foreground. Presenting App Open Ad...")
        currentActivity?.let { activity ->
            showAdIfAvailable(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) {
            currentActivity = activity
        }
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

    companion object {
        private const val TAG = "AppOpenAdManager"
    }
}
