package com.app.privacyscreendisplay.core.ads.engine

import com.app.privacyscreendisplay.core.ads.model.AdRevenueInfo
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.ResponseInfo

/**
 * Encapsulates AdMob OnPaidEventListener for impression revenue telemetry.
 */
class AdRevenueTracker(
    private val adType: String,
    private val adUnitId: String,
    private val getResponseInfo: () -> ResponseInfo? = { null }
) : OnPaidEventListener {

    private val TAG = "AdRevenueTracker"

    override fun onPaidEvent(adValue: AdValue) {
        val adapterClass = getResponseInfo()?.loadedAdapterResponseInfo?.adSourceInstanceName
        val revenueInfo = AdRevenueInfo(
            valueMicros = adValue.valueMicros,
            currencyCode = adValue.currencyCode,
            precisionType = adValue.precisionType,
            adUnitId = adUnitId,
            adNetworkAdapter = adapterClass,
            adType = adType
        )

        AdLogger.i(
            TAG,
            "REVENUE EVENT [$adType]: ${adValue.valueMicros} ${adValue.currencyCode} (Precision: ${adValue.precisionType}) | Network: $adapterClass | UnitID: $adUnitId"
        )
    }
}
