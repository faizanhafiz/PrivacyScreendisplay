package com.app.privacyscreendisplay.core.ads.config


import com.google.android.datatransport.BuildConfig

/**
 * AdMob + Meta Mediation Ad Unit Configuration Holder.
 * Automatically resolves between Official AdMob Test Ad Unit IDs (in Debug builds)
 * and Live Production AdMob Ad Unit IDs (in Release builds).
 */
object AdConfig {

    const val APP_ID = "ca-app-pub-7188839988485075~4446233778"

    const val is_test = true

    @Volatile
    var isPremiumUser: Boolean = false

    var WAITLIST_WEBHOOK_URL: String = "https://script.google.com/macros/s/AKfycbyA-CQU8ehPLLqrWxnEGX4jJ7J60D8OV0Oy-Dtvs7pLIQoW1BDupNou820Y3mYHYK_5/exec"

    // Official Google AdMob Test Ad Unit IDs
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    // Live Production AdMob Ad Unit IDs
    private const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-7188839988485075/3133152105"
    private const val PROD_NATIVE_AD_UNIT_ID = "ca-app-pub-7188839988485075/8883906309"
    private const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7188839988485075/2565909968"
    private const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-7188839988485075/1196987979"
    private const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-7188839988485075/9506988764"

    val BANNER_AD_UNIT_ID: String
        get() = if (is_test) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID

    val NATIVE_AD_UNIT_ID: String
        get() = if (is_test) TEST_NATIVE_AD_UNIT_ID else PROD_NATIVE_AD_UNIT_ID

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (is_test) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    val REWARDED_AD_UNIT_ID: String
        get() = if (is_test) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID

    val APP_OPEN_AD_UNIT_ID: String
        get() = if (is_test) TEST_APP_OPEN_AD_UNIT_ID else PROD_APP_OPEN_AD_UNIT_ID
}
