package com.app.privacyscreendisplay.core.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

/**
 * Singleton Ad Manager orchestrating SDK initialization for Google Mobile Ads (AdMob)
 * and Meta Audience Network Mediation adapter.
 */
object AdManager {

    private const val TAG = "AdManager"
    private var isInitialized = false

    /**
     * Initializes Google Mobile Ads SDK along with Meta Audience Network mediation adapters.
     *
     * @param context Application context.
     * @param onInitializationComplete Callback executed when ad SDK initialization completes.
     */
    fun initialize(
        context: Context,
        onInitializationComplete: () -> Unit = {}
    ) {
        if (isInitialized) {
            onInitializationComplete()
            return
        }

        // Configure test devices to ensure test ads are served reliably during development
        val testDeviceIds = listOf(
            AdRequest.DEVICE_ID_EMULATOR,
//            "A9FC6798D874A11556498DD641F83D7H" // Physical test device ID
        )
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        // Initialize Google Mobile Ads SDK (also initializes Meta Audience Network mediation)
        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapterClass, status) in statusMap) {
                Log.d(TAG, "Ad Adapter: $adapterClass, State: ${status.initializationState}, Description: ${status.description}")
            }
            isInitialized = true
            onInitializationComplete()
        }
    }

    /**
     * Builds a standard [AdRequest] configured for mediation.
     */
    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
