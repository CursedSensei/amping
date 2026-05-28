package com.pinghtdog.amping.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HealthGuideUiState(
    val providerName: String = "Nurse Sarah",
    val introMessage: String = "I'll be watching your progress. I'm proud of you for starting.",
    val providerAvatarUrl: String? = null
)

class HealthGuideViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HealthGuideUiState())
    val uiState: StateFlow<HealthGuideUiState> = _uiState.asStateFlow()
}
