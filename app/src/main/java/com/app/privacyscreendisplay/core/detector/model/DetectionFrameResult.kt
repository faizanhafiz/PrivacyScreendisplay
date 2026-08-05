package com.app.privacyscreendisplay.core.detector.model

/**
 * Output data class produced per analyzed camera frame.
 */
data class DetectionFrameResult(
    val scenario: ShoulderSurfingScenario,
    val smoothedConfidence: Float,           // Sliding-window smoothed confidence score (0.0 - 100.0)
    val maxRawConfidence: Float,             // Highest raw confidence among secondary faces in current frame
    val primaryUserAnalysis: FaceFeatureAnalysis?,
    val secondaryFaceAnalyses: List<FaceFeatureAnalysis>,
    val timestampMs: Long = System.currentTimeMillis(),
    val processingLatencyMs: Long = 0L
)
