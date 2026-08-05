package com.app.privacyscreendisplay.core.detector.evaluator

import com.app.privacyscreendisplay.core.detector.model.FaceFeatureAnalysis
import com.app.privacyscreendisplay.core.detector.model.HeadDirectionState
import com.app.privacyscreendisplay.core.detector.model.ShoulderSurfingScenario
import java.util.ArrayDeque

/**
 * Weighted Confidence Calculator and Temporal Sliding-Window Smoother.
 * Evaluates individual face threat metrics without face tracking IDs, and applies
 * a 4-frame sliding window to guarantee 200–300 ms response time with low false positives.
 */
class ConfidenceCalculator(
    private val windowSize: Int = 4,
    private val alertThreshold: Float = 65.0f,
    private val mediumRiskThreshold: Float = 45.0f
) {
    private val slidingWindowQueue = ArrayDeque<Float>(windowSize)

    /**
     * Calculates the raw weighted confidence score (0.0 to 100.0) for a secondary face.
     */
    fun calculateRawConfidence(
        headDirectionState: HeadDirectionState,
        gazeScore: Float,
        screenAlignmentScore: Float,
        estimatedDistanceMeters: Float,
        eyeOpenProbability: Float?
    ): Float {
        // If head direction or gaze indicates looking away / elsewhere, immediate 0 confidence
        if (headDirectionState == HeadDirectionState.LOOKING_AWAY ||
            headDirectionState == HeadDirectionState.DEFINITELY_ELSEWHERE ||
            gazeScore <= 0.0f
        ) {
            return 0.0f
        }

        val sHead = headDirectionState.confidenceWeight
        val sGaze = gazeScore
        val sAlign = screenAlignmentScore

        // Distance factor: Distance scales score slightly (0.85 at 2m, 1.0 at 0.5m), NEVER rejects
        val sDist = 1.0f - (0.15f * ((estimatedDistanceMeters - 0.5f) / 1.5f).coerceIn(0.0f, 1.0f))

        val sEye = (eyeOpenProbability ?: 1.0f).coerceIn(0.0f, 1.0f)

        // Weighted Formula: 40% Head Direction + 35% Gaze + 15% Alignment + 5% Distance + 5% Eye Open
        val rawScore = (0.40f * sHead + 0.35f * sGaze + 0.15f * sAlign + 0.05f * sDist + 0.05f * sEye) * 100.0f

        return rawScore.coerceIn(0.0f, 100.0f)
    }

    /**
     * Pushes a frame's peak secondary face raw confidence into the sliding window
     * and calculates the smoothed confidence score over the last 3-5 frames.
     */
    @Synchronized
    fun processFrameConfidence(maxFrameRawConfidence: Float): Pair<Float, ShoulderSurfingScenario> {
        if (slidingWindowQueue.size >= windowSize) {
            slidingWindowQueue.pollFirst()
        }
        slidingWindowQueue.addLast(maxFrameRawConfidence)

        // Weighted Moving Average over the last 3-5 frames (recent frames weighted higher)
        val windowList = slidingWindowQueue.toList()
        val smoothedConfidence = when (windowList.size) {
            1 -> windowList[0]
            2 -> windowList[1] * 0.60f + windowList[0] * 0.40f
            3 -> windowList[2] * 0.50f + windowList[1] * 0.30f + windowList[0] * 0.20f
            else -> { // 4 frames
                windowList[3] * 0.45f + windowList[2] * 0.30f + windowList[1] * 0.15f + windowList[0] * 0.10f
            }
        }

        val scenario = when {
            smoothedConfidence >= alertThreshold -> ShoulderSurfingScenario.ALERT
            smoothedConfidence >= mediumRiskThreshold -> ShoulderSurfingScenario.MEDIUM_RISK
            else -> ShoulderSurfingScenario.SAFE
        }

        return Pair(smoothedConfidence, scenario)
    }

    @Synchronized
    fun reset() {
        slidingWindowQueue.clear()
    }
}
