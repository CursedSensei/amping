package com.pinghtdog.amping.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StreakTutorialUiState(
    val activeDaysCount: Int = 0,
    val isFaqExpanded: Boolean = false,
    val gabbyMessage: String = "Every day you record your dose, your streak grows!"
)

class StreakTutorialViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StreakTutorialUiState())
    val uiState: StateFlow<StreakTutorialUiState> = _uiState.asStateFlow()

    init {
        startAnimation()
    }

    private fun startAnimation() {
        viewModelScope.launch {
            for (i in 1..7) {
                delay(800)
                _uiState.update { it.copy(activeDaysCount = i) }
            }
        }
    }

    fun toggleFaq() {
        _uiState.update { it.copy(isFaqExpanded = !it.isFaqExpanded) }
    }
}
