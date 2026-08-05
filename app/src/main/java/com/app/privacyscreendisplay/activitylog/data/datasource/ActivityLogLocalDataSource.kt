package com.app.privacyscreendisplay.activitylog.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.privacyscreendisplay.activitylog.domain.model.ActivityLogItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Context.activityLogDataStore: DataStore<Preferences> by preferencesDataStore(name = "activity_log_preferences")

/**
 * Local Data Source for managing persistent dynamic detection logs and activity history.
 */
class ActivityLogLocalDataSource(
    private val context: Context
) {
    private val KEY_LOGS_JSON = stringPreferencesKey("key_logs_json")
    private val KEY_INITIALIZED = stringPreferencesKey("key_initialized")

    /**
     * Emits detection history logs stored persistently in DataStore.
     */
    fun getActivityLogs(): Flow<List<ActivityLogItem>> {
        return context.activityLogDataStore.data.map { prefs ->
            val jsonString = prefs[KEY_LOGS_JSON]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                parseLogsJson(jsonString)
            }
        }
    }

    /**
     * Appends a new detection event dynamically to the persistent log store.
     */
    suspend fun logDetectionEvent(
        packageName: String,
        appName: String,
        extraFacesCount: Int = 1,
        durationSeconds: Int = 4,
        actionText: String = "Shoulder Surfer Blocked",
        imagePath: String? = null
    ) {
        context.activityLogDataStore.edit { prefs ->
            val existingJson = prefs[KEY_LOGS_JSON]
            val currentLogs = if (!existingJson.isNullOrEmpty()) {
                parseLogsJson(existingJson).toMutableList()
            } else {
                mutableListOf()
            }

            val timestamp = System.currentTimeMillis()
            val newItem = ActivityLogItem(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                appName = appName,
                timestamp = timestamp,
                formattedTime = formatTime(timestamp),
                dateGroup = formatDateGroup(timestamp),
                extraFacesCount = extraFacesCount,
                durationSeconds = durationSeconds,
                actionText = actionText,
                imagePath = imagePath,
                isUnblurred = false
            )

            // Add new log to the top of the list
            currentLogs.add(0, newItem)

            prefs[KEY_LOGS_JSON] = serializeLogsJson(currentLogs)
            prefs[KEY_INITIALIZED] = "true"
        }
    }

    suspend fun unblurLogItem(logId: String) {
        context.activityLogDataStore.edit { prefs ->
            val existingJson = prefs[KEY_LOGS_JSON]
            if (!existingJson.isNullOrEmpty()) {
                val currentLogs = parseLogsJson(existingJson).map { item ->
                    if (item.id == logId) {
                        item.copy(isUnblurred = true)
                    } else {
                        item
                    }
                }
                prefs[KEY_LOGS_JSON] = serializeLogsJson(currentLogs)
            }
        }
    }

    /**
     * Automatically purges logs older than the specified retention window (default 7 days)
     * and deletes their snapshot image files from disk.
     */
    suspend fun purgeLogsOlderThan(days: Int = 7) {
        val cutoffTimestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        context.activityLogDataStore.edit { prefs ->
            val existingJson = prefs[KEY_LOGS_JSON]
            if (!existingJson.isNullOrEmpty()) {
                val currentLogs = parseLogsJson(existingJson)
                val validLogs = mutableListOf<ActivityLogItem>()

                for (item in currentLogs) {
                    if (item.timestamp < cutoffTimestamp) {
                        item.imagePath?.let { path ->
                            try { java.io.File(path).delete() } catch (_: Exception) {}
                        }
                    } else {
                        validLogs.add(item)
                    }
                }
                prefs[KEY_LOGS_JSON] = serializeLogsJson(validLogs)
            }
        }
    }

    /**
     * Deletes a specific set of activity logs by their IDs and cleans up their snapshot files.
     */
    suspend fun deleteLogItems(logIds: Set<String>) {
        context.activityLogDataStore.edit { prefs ->
            val existingJson = prefs[KEY_LOGS_JSON]
            if (!existingJson.isNullOrEmpty()) {
                val currentLogs = parseLogsJson(existingJson)
                val remainingLogs = mutableListOf<ActivityLogItem>()

                for (item in currentLogs) {
                    if (logIds.contains(item.id)) {
                        item.imagePath?.let { path ->
                            try { java.io.File(path).delete() } catch (_: Exception) {}
                        }
                    } else {
                        remainingLogs.add(item)
                    }
                }
                prefs[KEY_LOGS_JSON] = serializeLogsJson(remainingLogs)
            }
        }
    }

    /**
     * Purges all activity logs persistently and deletes all associated snapshot image files.
     */
    suspend fun clearActivityLogs() {
        context.activityLogDataStore.edit { prefs ->
            val existingJson = prefs[KEY_LOGS_JSON]
            if (!existingJson.isNullOrEmpty()) {
                val currentLogs = parseLogsJson(existingJson)
                for (item in currentLogs) {
                    item.imagePath?.let { path ->
                        try { java.io.File(path).delete() } catch (_: Exception) {}
                    }
                }
            }
            prefs[KEY_LOGS_JSON] = ""
            prefs[KEY_INITIALIZED] = "true"
        }
    }

    private fun parseLogsJson(jsonString: String): List<ActivityLogItem> {
        val list = mutableListOf<ActivityLogItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val timestamp = obj.getLong("timestamp")
                list.add(
                    ActivityLogItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        packageName = obj.optString("packageName", ""),
                        appName = obj.optString("appName", "Protected App"),
                        timestamp = timestamp,
                        formattedTime = formatTime(timestamp),
                        dateGroup = formatDateGroup(timestamp),
                        extraFacesCount = obj.optInt("extraFacesCount", 1),
                        durationSeconds = obj.optInt("durationSeconds", 4),
                        actionText = obj.optString("actionText", "Shoulder Surfer Blocked"),
                        imagePath = if (obj.has("imagePath")) obj.getString("imagePath") else null,
                        isUnblurred = obj.optBoolean("isUnblurred", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun serializeLogsJson(logs: List<ActivityLogItem>): String {
        val jsonArray = JSONArray()
        for (item in logs) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("packageName", item.packageName)
            obj.put("appName", item.appName)
            obj.put("timestamp", item.timestamp)
            obj.put("extraFacesCount", item.extraFacesCount)
            obj.put("durationSeconds", item.durationSeconds)
            obj.put("actionText", item.actionText)
            item.imagePath?.let { obj.put("imagePath", it) }
            obj.put("isUnblurred", item.isUnblurred)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDateGroup(timestamp: Long): String {
        val logCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance()

        return if (logCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            logCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) {
            "Today"
        } else if (logCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            logCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) - 1) {
            "Yesterday"
        } else {
            "Earlier"
        }
    }
}
