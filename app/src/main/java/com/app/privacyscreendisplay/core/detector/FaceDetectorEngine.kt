package com.app.privacyscreendisplay.core.detector

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

/**
 * CameraX + ML Kit On-Device Face Detection Engine.
 * Features frame-rate throttling (~3 FPS) for battery efficiency, fast on-device face counting,
 * thread-safe volatile state synchronization, and 2.5s post-dismiss grace period.
 */
class FaceDetectorEngine(
    private val context: Context,
    private val onShoulderSurfingDetected: () -> Unit,
    private val onShoulderSurfingCleared: () -> Unit
) {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    // Configure ML Kit Face Detector in FAST performance mode for maximum speed and efficiency
    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(detectorOptions)

    // Battery-saving frame throttling (process 1 frame every 350ms -> ~3 FPS)
    @Volatile
    private var lastAnalyzedTimestamp = 0L
    private val frameIntervalMs = 350L

    // Post-Dismiss Grace Period (pause detection for 2.5s after user clicks Dismiss)
    @Volatile
    private var lastDismissTimestamp = 0L
    private val dismissGracePeriodMs = 2500L

    // Volatile state variables for thread-safe cross-thread visibility between UI and Camera threads
    @Volatile
    private var consecutiveMultipleFacesCount = 0

    @Volatile
    private var isCurrentlyAlerting = false

    private val DEBOUNCE_THRESHOLD = 3

    fun startDetection(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    analyzeFrame(imageProxy)
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopDetection() {
        try {
            cameraProvider?.unbindAll()
            consecutiveMultipleFacesCount = 0
            isCurrentlyAlerting = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(imageProxy: ImageProxy) {
        // Skip ML Kit analysis while privacy overlay is already active on screen
        if (isCurrentlyAlerting) {
            imageProxy.close()
            return
        }

        val currentTimestamp = System.currentTimeMillis()

        // Post-Dismiss Grace Period: Skip detection for 2.5s after user taps Dismiss
        if (currentTimestamp - lastDismissTimestamp < dismissGracePeriodMs) {
            imageProxy.close()
            return
        }

        // Frame Throttling: Skip frames if interval has not elapsed
        if (currentTimestamp - lastAnalyzedTimestamp < frameIntervalMs) {
            imageProxy.close()
            return
        }

        lastAnalyzedTimestamp = currentTimestamp
        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        try {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val faceCount = faces.size

                    // Testing threshold: trigger on 1+ detected face (revert to 2+ for production)
                    if (faceCount >= 1) {
                        consecutiveMultipleFacesCount++
                        if (consecutiveMultipleFacesCount >= DEBOUNCE_THRESHOLD && !isCurrentlyAlerting) {
                            isCurrentlyAlerting = true
                            onShoulderSurfingDetected()
                        }
                    } else {
                        // Reset consecutive count when face leaves
                        consecutiveMultipleFacesCount = 0
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            imageProxy.close()
        }
    }

    /**
     * Resets the alerting state after the user manually taps the Dismiss button,
     * activating a 2.5s post-dismiss grace period.
     */
    fun resetAlert() {
        isCurrentlyAlerting = false
        consecutiveMultipleFacesCount = 0
        lastDismissTimestamp = System.currentTimeMillis()
    }

    fun release() {
        stopDetection()
        detector.close()
        cameraExecutor.shutdown()
    }
}
