package com.app.privacyscreendisplay

import android.app.Application
import com.app.privacyscreendisplay.core.ads.AdManager
import com.app.privacyscreendisplay.core.ads.AppOpenAdManager

/**
 * Base Application class managing global SDK initializations including
 * Google Mobile Ads, Meta Mediation, and App Open Ad lifecycle observation.
 */
class PrivacyGuardApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize App Open Ad Manager at application scope
        appOpenAdManager = AppOpenAdManager(this)

        // Initialize Google Mobile Ads & Meta Mediation SDK
        AdManager.initialize(this) {
            appOpenAdManager.fetchAd(showOnLoad = true)
        }
    }
}
