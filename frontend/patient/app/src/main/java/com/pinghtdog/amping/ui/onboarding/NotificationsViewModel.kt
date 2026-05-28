package com.pinghtdog.amping.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class NotificationStyle(val displayName: String) {
    SOUND_BANNER("Sound + Banner"),
    BANNER_ONLY("Banner Only"),
    GABBY_VOICE("Gabby Voice Reminder")
}

enum class SurveyFrequency(val displayName: String) {
    WEEKLY("Weekly"),
    BI_WEEKLY("Bi-weekly")
}

data class NotificationsUiState(
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val selectedStyle: NotificationStyle = NotificationStyle.SOUND_BANNER,
    val selectedFrequency: SurveyFrequency = SurveyFrequency.WEEKLY
)

class NotificationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute) }
    }

    fun updateStyle(style: NotificationStyle) {
        _uiState.update { it.copy(selectedStyle = style) }
    }

    fun updateFrequency(frequency: SurveyFrequency) {
        _uiState.update { it.copy(selectedFrequency = frequency) }
    }
}
