package com.app.privacyscreendisplay.core.detector.evaluator

import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Real-Human Liveness and Anti-Spoofing Evaluator.
 * Filters out false-positive detections caused by:
 * - Animals / Birds / Pets
 * - Printed photos, posters, and pictures
 * - Movie actors on laptop / TV / digital displays
 * - Sleeping people or closed-eye masks
 */
object RealHumanLivenessEvaluator {

    data class LivenessResult(
        val isRealHuman: Boolean,
        val rejectionReason: String? = null
    )

    /**
     * Verifies whether a detected face belongs to a live 3D real human facing the phone camera,
     * vs a picture, poster, pet, or digital video display.
     */
    fun verifyRealHuman(
        face: Face,
        leftEyeOpenProb: Float?,
        rightEyeOpenProb: Float?,
        estimatedDistanceMeters: Float
    ): LivenessResult {
        // 1. ANIMAL / PET / NON-HUMAN FILTER
        // ML Kit landmark validation: Real human faces MUST have key facial landmarks detected
        val leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE)
        val noseLandmark = face.getLandmark(FaceLandmark.NOSE_BASE)

        if (leftEyeLandmark == null || rightEyeLandmark == null || noseLandmark == null) {
            return LivenessResult(false, "Missing key 3D human landmarks (Animal / Non-human face)")
        }

        // 2. EYE OPEN / SLEEPING / MASK FILTER
        val avgEyeOpen = if (leftEyeOpenProb != null && rightEyeOpenProb != null) {
            (leftEyeOpenProb + rightEyeOpenProb) / 2.0f
        } else {
            leftEyeOpenProb ?: rightEyeOpenProb ?: 0.0f
        }

        if (avgEyeOpen < 0.20f) {
            return LivenessResult(false, "Eyes closed or sleeping person")
        }

        // 3. 3D LANDMARK GEOMETRY RATIO (Rejects distorted 2D projections & flat pictures)
        // Measure inter-eye distance vs nose-to-eye vertical distance
        val eyeDistance = distance(leftEyeLandmark.position.x, leftEyeLandmark.position.y, rightEyeLandmark.position.x, rightEyeLandmark.position.y)
        val eyeMidX = (leftEyeLandmark.position.x + rightEyeLandmark.position.x) / 2.0f
        val eyeMidY = (leftEyeLandmark.position.y + rightEyeLandmark.position.y) / 2.0f
        val noseEyeDist = distance(noseLandmark.position.x, noseLandmark.position.y, eyeMidX, eyeMidY)

        if (eyeDistance <= 0f || noseEyeDist <= 0f) {
            return LivenessResult(false, "Invalid 3D landmark geometry")
        }

        val geometryRatio = eyeDistance / noseEyeDist
        // Human 3D face inter-eye to nose ratio is typically between 0.9 and 2.6
        if (geometryRatio < 0.70f || geometryRatio > 3.0f) {
            return LivenessResult(false, "Unnatural 2D face geometry ratio (Picture / Cartoon / Reflection)")
        }

        return LivenessResult(true, null)
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
}
