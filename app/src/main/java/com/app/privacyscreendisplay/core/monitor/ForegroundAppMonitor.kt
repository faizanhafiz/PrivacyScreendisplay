package com.app.privacyscreendisplay.core.monitor

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings

/**
 * Monitors foreground application state using Android UsageStatsManager.
 * Ensures CameraX face detection is armed ONLY when a user-protected app
 * (WhatsApp, PhonePe, Amazon, etc.) is active in the foreground, reducing battery drain.
 */
class ForegroundAppMonitor(
    private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    /**
     * Checks if Usage Access permission is granted in System Settings.
     */
    fun hasUsageAccessPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Prompts the user to grant Usage Access Permission.
     */
    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Returns the package name of the application currently in the foreground.
     */
    fun getForegroundPackageName(): String? {
        if (!hasUsageAccessPermission() || usageStatsManager == null) return null

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000L

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime) ?: return null
        val event = UsageEvents.Event()
        var lastForegroundPackage: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundPackage = event.packageName
            }
        }
        return lastForegroundPackage
    }

    /**
     * Checks if any app in [protectedPackages] is currently in the foreground.
     */
    fun isProtectedAppInForeground(protectedPackages: Set<String>): Boolean {
        if (protectedPackages.isEmpty()) return false
        val currentPackage = getForegroundPackageName() ?: return false
        return protectedPackages.contains(currentPackage)
    }
}
