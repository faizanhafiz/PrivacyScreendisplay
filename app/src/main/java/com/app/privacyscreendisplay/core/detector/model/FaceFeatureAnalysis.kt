package com.app.privacyscreendisplay.core.detector.model

import android.graphics.PointF
import android.graphics.Rect

/**
 * Detailed feature extraction and threat evaluation metrics calculated for a single face.
 */
data class FaceFeatureAnalysis(
    val boundingBox: Rect,
    val faceCenterNormalized: PointF, // Normalized (x, y) coordinates in range [0.0, 1.0]
    val faceSizeRatio: Float,          // Face bounding area relative to image dimensions
    val estimatedDistanceMeters: Float, // Estimated distance in meters (0.3m - 2.5m)
    val headYaw: Float,                // Euler Y: Head turn left (-) / right (+) in degrees
    val headPitch: Float,              // Euler X: Head tilt down (-) / up (+) in degrees
    val headRoll: Float,               // Euler Z: Head side tilt in degrees
    val eyeOpenProbability: Float,     // Average probability of eyes being open (0.0 to 1.0)
    val headDirectionState: HeadDirectionState,
    val screenAlignmentScore: Float,   // Alignment between head orientation & screen vector (0.0 to 1.0)
    val gazeScore: Float,              // Estimated gaze vector score (0.0 to 1.0)
    val rawConfidenceScore: Float,     // Weighted raw confidence (0.0 to 100.0)
    val isPrimaryUser: Boolean = false
)
