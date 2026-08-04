package com.app.privacyscreendisplay.activitylog.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.privacyscreendisplay.activitylog.data.repository.ActivityLogRepository
import com.app.privacyscreendisplay.activitylog.domain.model.ActivityLogItem
import com.app.privacyscreendisplay.core.ads.AdConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivityLogUiState(
    val isLoading: Boolean = true,
    val logs: List<ActivityLogItem> = emptyList(),
    val detectionsToday: Int = 0,
    val mostProtectedApp: String = "None",
    val totalActivations: Int = 0,
    val isPremiumUser: Boolean = false
)

class ActivityLogViewModel(
    private val repository: ActivityLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    init {
        observeLogs()
    }

    private fun observeLogs() {
        viewModelScope.launch {
            repository.getActivityLogs().collect { logList ->
                val todayCount = logList.count { it.dateGroup == "Today" }
                val mostApp = logList.groupBy { it.appName }
                    .maxByOrNull { it.value.size }?.key ?: "None"
                val totalActivations = logList.size

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        logs = logList,
                        detectionsToday = todayCount,
                        mostProtectedApp = mostApp,
                        totalActivations = totalActivations,
                        isPremiumUser = AdConfig.isPremiumUser
                    )
                }
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearActivityLogs()
        }
    }

    fun unblurLogItem(logId: String) {
        viewModelScope.launch {
            repository.unblurLogItem(logId)
        }
    }
}
