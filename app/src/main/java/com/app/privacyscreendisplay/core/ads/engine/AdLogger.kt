package com.app.privacyscreendisplay.core.ads.engine

import android.util.Log
import com.app.privacyscreendisplay.BuildConfig

/**
 * Centralized logging component for the Ads engine.
 * Verbose logging is enabled ONLY in Debug builds to prevent log spam in release builds.
 */
object AdLogger {

    private const val GLOBAL_TAG = "PrivacyGuard_Ads"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$GLOBAL_TAG:$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i("$GLOBAL_TAG:$tag", message)
        }
    }

    fun w(tag: String, message: String) {
        Log.w("$GLOBAL_TAG:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$GLOBAL_TAG:$tag", message, throwable)
    }
}
