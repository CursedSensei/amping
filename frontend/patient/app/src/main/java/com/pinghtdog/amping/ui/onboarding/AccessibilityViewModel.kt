package com.pinghtdog.amping.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AccessibilityUiState(
    val language: String = "English",
    val textSize: String = "Standard",
    val voiceMode: String = "On",
    val reminderTime: String = "08:00 AM",
    val profileType: String = "Adult"
)

class AccessibilityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AccessibilityUiState())
    val uiState: StateFlow<AccessibilityUiState> = _uiState.asStateFlow()
}
