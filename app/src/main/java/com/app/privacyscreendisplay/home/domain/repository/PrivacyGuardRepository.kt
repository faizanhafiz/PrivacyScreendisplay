package com.app.privacyscreendisplay.home.domain.repository

import com.app.privacyscreendisplay.home.domain.model.OverlayStyle
import com.app.privacyscreendisplay.home.domain.model.ProtectionStatus
import com.app.privacyscreendisplay.home.domain.model.SensitivityLevel
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract defining state operations and configuration management for Privacy Guard.
 */
interface PrivacyGuardRepository {

    /**
     * Observes real-time protection status flow.
     */
    fun getProtectionStatus(): Flow<ProtectionStatus>

    /**
     * Toggles the active state of the privacy protection service.
     *
     * @param active New active state.
     */
    suspend fun setProtectionActive(active: Boolean)

    /**
     * Updates the selected privacy overlay visual style.
     *
     * @param style The chosen [OverlayStyle].
     */
    suspend fun setOverlayStyle(style: OverlayStyle)

    /**
     * Updates the AI face detection sensitivity level.
     *
     * @param sensitivity The chosen [SensitivityLevel].
     */
    suspend fun setSensitivityLevel(sensitivity: SensitivityLevel)
}
