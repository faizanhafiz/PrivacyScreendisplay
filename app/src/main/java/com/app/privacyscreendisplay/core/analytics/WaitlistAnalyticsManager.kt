package com.app.privacyscreendisplay.core.analytics

import android.content.Context
import android.os.Build
import android.util.Log
import com.app.privacyscreendisplay.core.ads.AdConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Analytics Manager for tracking and recording user interest in Premium Subscriptions.
 * Logs structured analytics events to Logcat (tag: "WaitlistAnalytics") and dispatches
 * interest telemetry to an optional remote webhook endpoint for developer analysis.
 */
object WaitlistAnalyticsManager {

    private const val TAG = "WaitlistAnalytics"

    fun logWaitlistSignup(context: Context, email: String) {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"

        // 1. Structured Logcat Analytics for Developer Inspection (adb logcat -s WaitlistAnalytics)
        Log.i(TAG, "=========================================================")
        Log.i(TAG, "📊 [PREMIUM WAITLIST SIGNUP DETECTED]")
        Log.i(TAG, "   Email: $email")
        Log.i(TAG, "   Timestamp: $timeStamp")
        Log.i(TAG, "   Device: $deviceModel")
        Log.i(TAG, "=========================================================")

        // 2. Dispatch Remote Telemetry if webhook URL is configured in AdConfig
        val webhookUrl = AdConfig.WAITLIST_WEBHOOK_URL
        if (webhookUrl.isNotBlank()) {
            sendRemoteAnalytics(webhookUrl, email, timeStamp, deviceModel)
        }
    }

    private fun sendRemoteAnalytics(webhookUrl: String, email: String, timeStamp: String, deviceModel: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var currentUrl = webhookUrl
                var redirectCount = 0
                var responseCode = -1

                val postData = "email=" + java.net.URLEncoder.encode(email, "UTF-8") +
                        "&timestamp=" + java.net.URLEncoder.encode(timeStamp, "UTF-8") +
                        "&device=" + java.net.URLEncoder.encode(deviceModel, "UTF-8")
                val postDataBytes = postData.toByteArray(Charsets.UTF_8)

                while (redirectCount < 5) {
                    val url = URL(currentUrl)
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        instanceFollowRedirects = true
                        setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                        setRequestProperty("Content-Length", postDataBytes.size.toString())
                        doOutput = true
                        connectTimeout = 10000
                        readTimeout = 10000
                    }

                    connection.outputStream.use { os ->
                        os.write(postDataBytes)
                    }

                    responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == 302 || responseCode == 303 ||
                        responseCode == 307 || responseCode == 308) {
                        val newUrl = connection.getHeaderField("Location")
                        if (newUrl != null) {
                            currentUrl = newUrl
                            redirectCount++
                            continue
                        }
                    }
                    break
                }

                Log.d(TAG, "Remote waitlist analytics POST response code: $responseCode for $email")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send remote waitlist analytics: ${e.message}", e)
            }
        }
    }
}
