package com.app.privacyscreendisplay.core.ads.model

/**
 * Encapsulates revenue telemetry captured by AdMob OnPaidEventListener.
 */
data class AdRevenueInfo(
    val valueMicros: Long,
    val currencyCode: String,
    val precisionType: Int,
    val adUnitId: String,
    val adNetworkAdapter: String?,
    val adType: String
)
