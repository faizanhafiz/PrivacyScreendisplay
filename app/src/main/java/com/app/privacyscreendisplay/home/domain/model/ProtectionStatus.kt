package com.app.privacyscreendisplay.home.domain.model

/**
 * Domain entity model capturing the overall real-time privacy protection state.
 *
 * @property isProtectionActive `true` if AI face detection service is active, `false` otherwise.
 * @property selectedOverlayStyle Currently chosen overlay style.
 * @property sensitivity Current AI sensitivity level.
 * @property protectedAppsCount Total count of user-selected protected applications.
 * @property detectionsToday Total shoulder-surfer detection events logged today.
 * @property isPremiumSubscriber Indicates whether user holds an active Premium Google Play subscription.
 */
data class ProtectionStatus(
    val isProtectionActive: Boolean = true,
    val selectedOverlayStyle: OverlayStyle = OverlayStyle.BLUR,
    val sensitivity: SensitivityLevel = SensitivityLevel.HIGH,
    val protectedAppsCount: Int = 12,
    val detectionsToday: Int = 0,
    val isPremiumSubscriber: Boolean = false,
    val recentLogs: List<com.app.privacyscreendisplay.activitylog.domain.model.ActivityLogItem> = emptyList()
)
