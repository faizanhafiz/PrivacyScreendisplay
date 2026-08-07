package com.app.privacyscreendisplay.core.ads.engine

import kotlin.math.min
import kotlin.math.pow

/**
 * Manages exponential backoff retry intervals for failed ad requests.
 * Intervals: 2s -> 4s -> 8s -> 16s -> 30s -> 60s (capped at 60s max).
 */
class AdRetryPolicy(
    private val initialDelayMs: Long = 2000L,
    private val maxDelayMs: Long = 60000L,
    private val multiplier: Double = 2.0
) {
    private var retryCount = 0

    @Synchronized
    fun getNextDelayMs(): Long {
        val delay = (initialDelayMs * multiplier.pow(retryCount.toDouble())).toLong()
        retryCount++
        return min(delay, maxDelayMs)
    }

    @Synchronized
    fun reset() {
        retryCount = 0
    }

    @Synchronized
    fun getRetryCount(): Int = retryCount
}
