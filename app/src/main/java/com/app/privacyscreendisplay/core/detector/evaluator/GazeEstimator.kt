package com.app.privacyscreendisplay.core.detector.evaluator

import com.app.privacyscreendisplay.core.detector.model.HeadDirectionState
import kotlin.math.abs
import kotlin.math.exp

/**
 * Geometric Gaze and Head Direction Estimator.
 * Combines ML Kit Euler angles (Yaw, Pitch, Roll), eye openness, and camera coordinate space
 * to estimate whether a face is intentionally directed at the user's phone screen.
 */
object GazeEstimator {

    data class GazeEvaluationResult(
        val headDirectionState: HeadDirectionState,
        val gazeScore: Float // Normalized score between 0.0 and 1.0
    )

    fun evaluateGaze(
        headYaw: Float,
        headPitch: Float,
        headRoll: Float,
        eyeOpenProbability: Float?,
        faceCenterXNormalized: Float,
        faceCenterYNormalized: Float,
        estimatedDistanceMeters: Float = 1.0f
    ): GazeEvaluationResult {
        val eyeOpen = eyeOpenProbability ?: 1.0f

        // Hard exclusion 1: Eyes closed or sleeping
        if (eyeOpen < 0.20f) {
            return GazeEvaluationResult(HeadDirectionState.DEFINITELY_ELSEWHERE, 0.0f)
        }

        // Hard exclusion 2: Head looking down (at own phone/hands) or up (ceiling)
        if (headPitch < -30.0f || headPitch > 30.0f) {
            return GazeEvaluationResult(HeadDirectionState.DEFINITELY_ELSEWHERE, 0.0f)
        }

        // Hard exclusion 3: Head turned far sideways (>40 degrees away)
        if (abs(headYaw) > 40.0f) {
            return GazeEvaluationResult(HeadDirectionState.LOOKING_AWAY, 0.0f)
        }

        // Calculate expected Yaw & Pitch required for a face at (X_norm, Y_norm) to intersect screen center (0.5, 0.5)
        val expectedYaw = (0.5f - faceCenterXNormalized) * 55.0f
        val expectedPitch = (faceCenterYNormalized - 0.40f) * 30.0f

        val yawError = abs(headYaw - expectedYaw)
        val pitchError = abs(headPitch - expectedPitch)

        // Distance-Adaptive Angular Error Limits:
        // People 1.2m+ away (e.g. laptop/TV screens 30cm to the side) must be aligned within tighter angles
        val (maxYawLook, maxPitchLook) = when {
            estimatedDistanceMeters <= 0.7f -> Pair(12.0f, 14.0f)
            estimatedDistanceMeters <= 1.2f -> Pair(8.0f, 10.0f)
            else -> Pair(6.0f, 7.0f) // Long distance (>1.2m): Rejects laptop/TV screen angle offsets (>10 deg)
        }

        val (maxYawProb, maxPitchProb) = when {
            estimatedDistanceMeters <= 0.7f -> Pair(22.0f, 22.0f)
            estimatedDistanceMeters <= 1.2f -> Pair(15.0f, 16.0f)
            else -> Pair(11.0f, 12.0f)
        }

        // Classify Head Direction State based on distance-adaptive angular error margins
        val directionState = when {
            yawError <= maxYawLook && pitchError <= maxPitchLook -> HeadDirectionState.LOOKING_AT_SCREEN
            yawError <= maxYawProb && pitchError <= maxPitchProb -> HeadDirectionState.PROBABLY_LOOKING
            yawError <= 30.0f -> HeadDirectionState.NEUTRAL
            else -> HeadDirectionState.LOOKING_AWAY
        }

        // Exponential decay for angular error precision
        val angularDecay = exp(-(yawError + pitchError) / 20.0f)
        val eyeModifier = eyeOpen.coerceIn(0.5f, 1.0f)
        val gazeScore = (directionState.confidenceWeight * angularDecay * eyeModifier).coerceIn(0.0f, 1.0f)

        return GazeEvaluationResult(directionState, gazeScore)
    }
}
