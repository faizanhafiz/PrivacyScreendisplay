package com.app.privacyscreendisplay.activitylog.data.repository

import com.app.privacyscreendisplay.activitylog.data.datasource.ActivityLogLocalDataSource
import com.app.privacyscreendisplay.activitylog.domain.model.ActivityLogItem
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {
    fun getActivityLogs(): Flow<List<ActivityLogItem>>
    suspend fun clearActivityLogs()
    suspend fun deleteLogItems(logIds: Set<String>)
    suspend fun purgeLogsOlderThan(days: Int)
    suspend fun unblurLogItem(logId: String)
}

class ActivityLogRepositoryImpl(
    private val localDataSource: ActivityLogLocalDataSource
) : ActivityLogRepository {

    override fun getActivityLogs(): Flow<List<ActivityLogItem>> {
        return localDataSource.getActivityLogs()
    }

    override suspend fun clearActivityLogs() {
        localDataSource.clearActivityLogs()
    }

    override suspend fun deleteLogItems(logIds: Set<String>) {
        localDataSource.deleteLogItems(logIds)
    }

    override suspend fun purgeLogsOlderThan(days: Int) {
        localDataSource.purgeLogsOlderThan(days)
    }

    override suspend fun unblurLogItem(logId: String) {
        localDataSource.unblurLogItem(logId)
    }
}
