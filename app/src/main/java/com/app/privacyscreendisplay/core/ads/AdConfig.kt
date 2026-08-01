package com.app.privacyscreendisplay.core.ads

/**
 * Configuration holder for AdMob + Meta Mediation Ad Unit IDs.
 *
 * NOTE: Currently uses official Google AdMob Test Ad Unit IDs.
 * Replace the production placeholders with your real AdMob & Meta dashboard IDs before publishing to Google Play.
 */
object AdConfig {

    // Global premium status toggle - set to true to test Premium Mode across all screens
    @Volatile
    var isPremiumUser: Boolean = true

    // Toggle this flag to switch between official Test IDs and Production IDs
    const val IS_TEST_MODE = true

    // Official AdMob Test App ID & Ad Unit IDs
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    // Production Placeholders (Replace with your actual AdMob Dashboard IDs)
    private const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-YOUR_ADMOB_PUB_ID/YOUR_BANNER_ID"
    private const val PROD_NATIVE_AD_UNIT_ID = "ca-app-pub-YOUR_ADMOB_PUB_ID/YOUR_NATIVE_ID"
    private const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-YOUR_ADMOB_PUB_ID/YOUR_INTERSTITIAL_ID"
    private const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-YOUR_ADMOB_PUB_ID/YOUR_REWARDED_ID"
    private const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-YOUR_ADMOB_PUB_ID/YOUR_APP_OPEN_ID"

    val bannerAdUnitId: String
        get() = if (IS_TEST_MODE) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID

    val nativeAdUnitId: String
        get() = if (IS_TEST_MODE) TEST_NATIVE_AD_UNIT_ID else PROD_NATIVE_AD_UNIT_ID

    val interstitialAdUnitId: String
        get() = if (IS_TEST_MODE) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    val rewardedAdUnitId: String
        get() = if (IS_TEST_MODE) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID

    val appOpenAdUnitId: String
        get() = if (IS_TEST_MODE) TEST_APP_OPEN_AD_UNIT_ID else PROD_APP_OPEN_AD_UNIT_ID
}
