package com.app.privacyscreendisplay.core.detector.evaluator

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Screen Alignment Evaluator.
 * Computes 3D vector alignment between a face's orientation normal vector
 * and the 3D ray pointing from the face center to the phone screen plane.
 */
object ScreenAlignmentEvaluator {

    fun evaluateAlignment(
        faceCenterXNormalized: Float,
        faceCenterYNormalized: Float,
        headYawDegrees: Float,
        headPitchDegrees: Float
    ): Float {
        // Vector from face center (X_norm, Y_norm, Z_approx) to screen center (0.5, 0.5, 0.0)
        val dirX = 0.5f - faceCenterXNormalized
        val dirY = 0.5f - faceCenterYNormalized
        val dirZ = -0.8f // Camera plane distance in normalized 3D space

        val lengthScreen = sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
        val nxScreen = dirX / lengthScreen
        val nyScreen = dirY / lengthScreen
        val nzScreen = dirZ / lengthScreen

        // Convert Euler angles (yaw, pitch) to face orientation normal vector
        val yawRad = Math.toRadians(headYawDegrees.toDouble()).toFloat()
        val pitchRad = Math.toRadians(headPitchDegrees.toDouble()).toFloat()

        val nxFace = sin(yawRad) * cos(pitchRad)
        val nyFace = -sin(pitchRad)
        val nzFace = -cos(yawRad) * cos(pitchRad)

        // 3D Dot Product gives cosine of intersection angle
        val dotProduct = nxScreen * nxFace + nyScreen * nyFace + nzScreen * nzFace

        return dotProduct.coerceIn(0.0f, 1.0f)
    }
}
