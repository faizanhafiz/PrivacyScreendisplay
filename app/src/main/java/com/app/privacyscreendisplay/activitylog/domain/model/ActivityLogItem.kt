package com.app.privacyscreendisplay.activitylog.domain.model

/**
 * Domain model representing a single detection event in the Activity Log.
 */
data class ActivityLogItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    val formattedTime: String,
    val dateGroup: String, // "Today", "Yesterday", "Earlier"
    val extraFacesCount: Int,
    val durationSeconds: Int,
    val actionText: String,
    val imagePath: String? = null,
    val isUnblurred: Boolean = false
)
