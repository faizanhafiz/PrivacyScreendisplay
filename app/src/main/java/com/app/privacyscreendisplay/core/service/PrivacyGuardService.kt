package com.app.privacyscreendisplay.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import androidx.core.content.ContextCompat
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.app.privacyscreendisplay.MainActivity
import com.app.privacyscreendisplay.R
import com.app.privacyscreendisplay.core.detector.FaceDetectorEngine
import com.app.privacyscreendisplay.core.monitor.ForegroundAppMonitor
import com.app.privacyscreendisplay.core.overlay.SystemOverlayManager
import com.app.privacyscreendisplay.home.data.datasource.PrivacyGuardLocalDataSource
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.protectedapps.data.datasource.ProtectedAppsLocalDataSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel

/**
 * Android 14+ Compliant Foreground Service for real-time Privacy Guard protection.
 * Runs CameraX + ML Kit face detection ONLY when a user-added protected app (WhatsApp, PhonePe, Amazon, etc.)
 * is in the foreground, and displays a system-wide WindowManager privacy overlay when shoulder surfing is detected.
 */
class PrivacyGuardService : LifecycleService() {

    private lateinit var overlayManager: SystemOverlayManager
    private lateinit var appMonitor: ForegroundAppMonitor
    private lateinit var shoulderSurfingEngine: com.app.privacyscreendisplay.core.detector.ShoulderSurfingEngine
    private lateinit var privacyGuardDS: PrivacyGuardLocalDataSource
    private lateinit var protectedAppsDS: ProtectedAppsLocalDataSource
    private lateinit var activityLogDS: com.app.privacyscreendisplay.activitylog.data.datasource.ActivityLogLocalDataSource

    private var monitorJob: Job? = null
    private var protectedPackages = setOf<String>()
    private var isProtectionActive = false
    private var selectedOverlayStyle = OverlayStyle.BLUR
    private var selectedSensitivity = SensitivityLevel.HIGH
    private var isCameraArmed = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "privacy_guard_service_channel"

        fun startService(context: Context) {
            val intent = Intent(context, PrivacyGuardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PrivacyGuardService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        overlayManager = SystemOverlayManager(this)
        appMonitor = ForegroundAppMonitor(this)
        privacyGuardDS = PrivacyGuardLocalDataSource(this)
        protectedAppsDS = ProtectedAppsLocalDataSource(this)
        activityLogDS = com.app.privacyscreendisplay.activitylog.data.datasource.ActivityLogLocalDataSource(this)

        shoulderSurfingEngine = com.app.privacyscreendisplay.core.detector.ShoulderSurfingEngine(
            context = this,
            onShoulderSurfingDetected = { snapshotPath ->
                if (isProtectionActive) {
                    val currentPkg = appMonitor.getForegroundPackageName() ?: "com.app.privacyscreendisplay"
                    val appName = try {
                        val pm = packageManager
                        val appInfo = pm.getApplicationInfo(currentPkg, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (_: Exception) {
                        "Protected App"
                    }

                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        activityLogDS.logDetectionEvent(
                            packageName = currentPkg,
                            appName = appName,
                            extraFacesCount = 1,
                            durationSeconds = 4,
                            actionText = "Shoulder Surfer Blocked",
                            imagePath = snapshotPath
                        )
                    }

                    overlayManager.showOverlay(
                        overlayStyle = selectedOverlayStyle,
                        onDismiss = {
                            overlayManager.hideOverlay()
                            shoulderSurfingEngine.resetAlert()
                        }
                    )
                }
            },
            onShoulderSurfingCleared = {
                // Privacy overlay persists until user taps dismiss
            }
        )

        startForegroundNotification()
        observeProtectionState()
    }

    private fun startForegroundNotification() {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shoulder Surfing Guard")
            .setContentText("Real-time AI camera screen privacy monitoring active")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (hasCameraPermission) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
                } else {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (inner: Exception) {
                inner.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Privacy Guard Protection Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification displayed while Privacy Guard is actively protecting your screen."
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun observeProtectionState() {
        lifecycleScope.launch {
            combine(
                privacyGuardDS.protectionStatusFlow,
                protectedAppsDS.getProtectedApps()
            ) { status, apps ->
                isProtectionActive = status.isProtectionActive
                selectedOverlayStyle = status.selectedOverlayStyle
                selectedSensitivity = status.sensitivity
                protectedPackages = apps.map { it.packageName }.toSet()
            }.collect {
                updateMonitoringState()
            }
        }
    }

    private fun updateMonitoringState() {
        monitorJob?.cancel()

        if (!isProtectionActive || protectedPackages.isEmpty()) {
            if (overlayManager.isShowing()) {
                overlayManager.hideOverlay()
                shoulderSurfingEngine.resetAlert()
            }
            disarmCamera()
            return
        }

        monitorJob = lifecycleScope.launch {
            while (true) {
                val isProtectedAppActive = appMonitor.isProtectedAppInForeground(protectedPackages)

                if (isProtectedAppActive) {
                    if (selectedSensitivity == SensitivityLevel.HIGH) {
                        if (!isCameraArmed) armCamera()
                        delay(1000L)
                    } else {
                        // Eco & Medium modes: Burst scan then disarm to turn off green camera status dot
                        armCamera()
                        delay(selectedSensitivity.scanDurationMs)

                        if (!overlayManager.isShowing()) {
                            disarmCamera() // Camera disarms -> Green dot turns OFF!
                            delay(selectedSensitivity.pauseDurationMs)
                        } else {
                            delay(1000L)
                        }
                    }
                } else {
                    // Automatically hide overlay when protected app is closed or exits foreground
                    if (overlayManager.isShowing()) {
                        overlayManager.hideOverlay()
                        shoulderSurfingEngine.resetAlert()
                    }
                    if (isCameraArmed) {
                        disarmCamera()
                    }
                    delay(1800L)
                }
            }
        }
    }

    private fun armCamera() {
        if (isCameraArmed) return
        try {
            shoulderSurfingEngine.startDetection(this)
            isCameraArmed = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disarmCamera() {
        if (!isCameraArmed) return
        try {
            shoulderSurfingEngine.stopDetection()
            isCameraArmed = false
            if (overlayManager.isShowing()) {
                overlayManager.hideOverlay()
                shoulderSurfingEngine.resetAlert()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        disarmCamera()
        shoulderSurfingEngine.release()
    }
}
