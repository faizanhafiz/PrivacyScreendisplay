package com.app.privacyscreendisplay.core.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.app.privacyscreendisplay.core.detector.evaluator.ConfidenceCalculator
import com.app.privacyscreendisplay.core.detector.evaluator.GazeEstimator
import com.app.privacyscreendisplay.core.detector.evaluator.ScreenAlignmentEvaluator
import com.app.privacyscreendisplay.core.detector.model.DetectionFrameResult
import com.app.privacyscreendisplay.core.detector.model.FaceFeatureAnalysis
import com.app.privacyscreendisplay.core.detector.model.ShoulderSurfingScenario
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.math.sqrt

/**
 * Production-Ready High-Accuracy Shoulder Surfing Detection Engine (No Face Tracking).
 *
 * Key Architecture Principles:
 * - NO FACE TRACKING: Analyzes each frame independently without trackingId, Kalman filters, or persistent IDs.
 * - TEMPORAL SLIDING WINDOW: Uses a 4-frame sliding window to smooth confidence, achieving 200-300 ms response.
 * - GEOMETRIC GAZE & ALIGNMENT: Combines Euler Yaw/Pitch/Roll, 3D screen vector alignment, and distance estimation.
 * - ZERO FALSE-POSITIVE LOGIC: Filters out looking away, looking at own phone (pitch < -35 deg), sleeping, and primary device user.
 */
class ShoulderSurfingEngine(
    private val context: Context,
    private val minFacesToAlert: Int = 2, // Set to 2 for production shoulder surfing, 1 for single-person testing
    private val onShoulderSurfingDetected: (snapshotPath: String?) -> Unit = {},
    private val onShoulderSurfingCleared: () -> Unit = {}
) {
    companion object {
        private const val TAG = "ShoulderSurfingEngine"
        private const val FRAME_INTERVAL_MS = 60L // Target 15-20 FPS (~60ms interval) for 200-300ms reaction
        private const val DISMISS_GRACE_PERIOD_MS = 2500L
    }

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    // High-performance ML Kit Face Detector with Euler pose, landmark, and classification modes enabled
    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.08f) // Detect secondary faces up to 2m away
        .build()

    private val detector = FaceDetection.getClient(detectorOptions)
    private val confidenceCalculator = ConfidenceCalculator(windowSize = 4, alertThreshold = 65.0f)

    private val _detectionResultState = MutableStateFlow(
        DetectionFrameResult(
            scenario = ShoulderSurfingScenario.SAFE,
            smoothedConfidence = 0.0f,
            maxRawConfidence = 0.0f,
            primaryUserAnalysis = null,
            secondaryFaceAnalyses = emptyList()
        )
    )
    val detectionResultState: StateFlow<DetectionFrameResult> = _detectionResultState.asStateFlow()

    @Volatile
    private var lastAnalyzedTimestamp = 0L

    @Volatile
    private var lastDismissTimestamp = 0L

    @Volatile
    private var isCurrentlyAlerting = false

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
                Log.d(TAG, "Shoulder Surfing Detection Engine started successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting CameraX detection engine: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopDetection() {
        ContextCompat.getMainExecutor(context).execute {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping CameraX detection engine: ${e.message}", e)
            } finally {
                confidenceCalculator.reset()
                isCurrentlyAlerting = false
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val startTimeMs = System.currentTimeMillis()

        if (isCurrentlyAlerting) {
            imageProxy.close()
            return
        }

        // Post-Dismiss Grace Period check
        if (startTimeMs - lastDismissTimestamp < DISMISS_GRACE_PERIOD_MS) {
            imageProxy.close()
            return
        }

        // Frame Throttling (~15-20 FPS)
        if (startTimeMs - lastAnalyzedTimestamp < FRAME_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastAnalyzedTimestamp = startTimeMs

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        try {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            val imgWidth = inputImage.width.toFloat()
            val imgHeight = inputImage.height.toFloat()
            val totalImgArea = imgWidth * imgHeight

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    processFaces(faces, totalImgArea, imgWidth, imgHeight, imageProxy, startTimeMs)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ML Kit Face Detection error: ${e.message}", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during frame analysis: ${e.message}", e)
            imageProxy.close()
        }
    }

    private fun processFaces(
        faces: List<Face>,
        totalImgArea: Float,
        imgWidth: Float,
        imgHeight: Float,
        imageProxy: ImageProxy,
        startTimeMs: Long
    ) {
        if (faces.isEmpty()) {
            val (smoothedConf, scenario) = confidenceCalculator.processFrameConfidence(0.0f)
            _detectionResultState.value = DetectionFrameResult(
                scenario = scenario,
                smoothedConfidence = smoothedConf,
                maxRawConfidence = 0.0f,
                primaryUserAnalysis = null,
                secondaryFaceAnalyses = emptyList(),
                processingLatencyMs = System.currentTimeMillis() - startTimeMs
            )
            return
        }

        // Analyze EVERY face in the frame uniformly using identical geometric gaze & alignment criteria
        val allFaceAnalyses = faces.map { face ->
            analyzeFaceFeature(
                face = face,
                totalImgArea = totalImgArea,
                imgWidth = imgWidth,
                imgHeight = imgHeight
            )
        }

        // Filter faces that are actively looking at the screen (rawConfidenceScore >= 40.0f)
        val facesLookingAtScreen = allFaceAnalyses.filter { it.rawConfidenceScore >= 40.0f }

        // Safely evaluate confidence according to minFacesToAlert requirement
        val sortedByConfidence = facesLookingAtScreen.sortedByDescending { it.rawConfidenceScore }
        val frameRawConfidence = if (facesLookingAtScreen.size >= minFacesToAlert) {
            val targetIndex = (minFacesToAlert - 1).coerceAtLeast(0)
            sortedByConfidence.getOrNull(targetIndex)?.rawConfidenceScore ?: 0.0f
        } else {
            0.0f
        }

        // Apply Sliding-Window Temporal Smoothing over 4 frames
        val (smoothedConfidence, scenario) = confidenceCalculator.processFrameConfidence(frameRawConfidence)

        _detectionResultState.value = DetectionFrameResult(
            scenario = scenario,
            smoothedConfidence = smoothedConfidence,
            maxRawConfidence = frameRawConfidence,
            primaryUserAnalysis = allFaceAnalyses.maxByOrNull { it.faceSizeRatio },
            secondaryFaceAnalyses = allFaceAnalyses,
            processingLatencyMs = System.currentTimeMillis() - startTimeMs
        )

        // Trigger Alert Callback if threat confirmed
        if (scenario.isAlertRequired && !isCurrentlyAlerting) {
            isCurrentlyAlerting = true
            val snapshotPath = saveFrameSnapshot(imageProxy)
            Log.w(TAG, "ALERT: Confirmed Shoulder Surfer detected! ${facesLookingAtScreen.size} faces looking at screen. Confidence=$smoothedConfidence%")
            onShoulderSurfingDetected(snapshotPath)
        }
    }

    private fun analyzeFaceFeature(
        face: Face,
        totalImgArea: Float,
        imgWidth: Float,
        imgHeight: Float
    ): FaceFeatureAnalysis {
        val bbox = face.boundingBox
        val faceArea = (bbox.width() * bbox.height()).toFloat()
        val faceSizeRatio = (faceArea / totalImgArea).coerceIn(0.001f, 1.0f)

        val centerXNorm = (bbox.centerX() / imgWidth).coerceIn(0.0f, 1.0f)
        val centerYNorm = (bbox.centerY() / imgHeight).coerceIn(0.0f, 1.0f)

        // Estimated Distance: d = 0.18 / sqrt(faceSizeRatio) meters
        val estimatedDistanceMeters = (0.18f / sqrt(faceSizeRatio)).coerceIn(0.3f, 2.5f)

        val yaw = face.headEulerAngleY   // Left (-) / Right (+)
        val pitch = face.headEulerAngleX // Down (-) / Up (+)
        val roll = face.headEulerAngleZ  // Side tilt

        val leftEyeProb = face.leftEyeOpenProbability
        val rightEyeProb = face.rightEyeOpenProbability
        val avgEyeOpenProb = if (leftEyeProb != null && rightEyeProb != null) {
            (leftEyeProb + rightEyeProb) / 2.0f
        } else {
            leftEyeProb ?: rightEyeProb
        }

        // 1. Verify Real Human Liveness (Rejects animals, pets, pictures, posters, sleeping faces)
        val livenessResult = com.app.privacyscreendisplay.core.detector.evaluator.RealHumanLivenessEvaluator.verifyRealHuman(
            face = face,
            leftEyeOpenProb = leftEyeProb,
            rightEyeOpenProb = rightEyeProb,
            estimatedDistanceMeters = estimatedDistanceMeters
        )

        val gazeResult = if (!livenessResult.isRealHuman) {
            com.app.privacyscreendisplay.core.detector.evaluator.GazeEstimator.GazeEvaluationResult(
                headDirectionState = com.app.privacyscreendisplay.core.detector.model.HeadDirectionState.DEFINITELY_ELSEWHERE,
                gazeScore = 0.0f
            )
        } else {
            GazeEstimator.evaluateGaze(
                headYaw = yaw,
                headPitch = pitch,
                headRoll = roll,
                eyeOpenProbability = avgEyeOpenProb,
                faceCenterXNormalized = centerXNorm,
                faceCenterYNormalized = centerYNorm,
                estimatedDistanceMeters = estimatedDistanceMeters
            )
        }

        val alignmentScore = ScreenAlignmentEvaluator.evaluateAlignment(
            faceCenterXNormalized = centerXNorm,
            faceCenterYNormalized = centerYNorm,
            headYawDegrees = yaw,
            headPitchDegrees = pitch
        )

        val rawConfidence = if (!livenessResult.isRealHuman) {
            0.0f
        } else {
            confidenceCalculator.calculateRawConfidence(
                headDirectionState = gazeResult.headDirectionState,
                gazeScore = gazeResult.gazeScore,
                screenAlignmentScore = alignmentScore,
                estimatedDistanceMeters = estimatedDistanceMeters,
                eyeOpenProbability = avgEyeOpenProb
            )
        }

        return FaceFeatureAnalysis(
            boundingBox = bbox,
            faceCenterNormalized = PointF(centerXNorm, centerYNorm),
            faceSizeRatio = faceSizeRatio,
            estimatedDistanceMeters = estimatedDistanceMeters,
            headYaw = yaw,
            headPitch = pitch,
            headRoll = roll,
            eyeOpenProbability = avgEyeOpenProb ?: 1.0f,
            headDirectionState = gazeResult.headDirectionState,
            screenAlignmentScore = alignmentScore,
            gazeScore = gazeResult.gazeScore,
            rawConfidenceScore = rawConfidence
        )
    }

    private fun saveFrameSnapshot(imageProxy: ImageProxy): String? {
        return try {
            val dir = File(context.filesDir, "intruder_photos")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "snapshot_${System.currentTimeMillis()}.jpg")

            val bitmap = imageProxy.toBitmap()
            val rotatedBitmap = if (imageProxy.imageInfo.rotationDegrees != 0) {
                val matrix = Matrix()
                matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            FileOutputStream(file).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving intruder snapshot: ${e.message}", e)
            null
        }
    }

    fun resetAlert() {
        isCurrentlyAlerting = false
        confidenceCalculator.reset()
        lastDismissTimestamp = System.currentTimeMillis()
    }

    fun release() {
        stopDetection()
        try {
            detector.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ML Kit Face Detector: ${e.message}", e)
        }
        try {
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down camera executor: ${e.message}", e)
        }
    }
}
