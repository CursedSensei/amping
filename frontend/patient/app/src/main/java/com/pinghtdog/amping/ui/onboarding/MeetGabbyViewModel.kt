package com.pinghtdog.amping.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MeetGabbyUiState(
    val interactionCount: Int = 0,
    val gabbyMessage: String = "Hi! I'm Gabby. I'm here to help you stay healthy!",
    val isInteractionComplete: Boolean = false
)

class MeetGabbyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MeetGabbyUiState())
    val uiState: StateFlow<MeetGabbyUiState> = _uiState.asStateFlow()

    fun onGabbyTapped() {
        if (_uiState.value.isInteractionComplete) return

        val newCount = _uiState.value.interactionCount + 1
        _uiState.update { 
            it.copy(
                interactionCount = newCount,
                gabbyMessage = when(newCount) {
                    1 -> "It's so good to meet you! Give me two more taps!"
                    2 -> "Almost there! One more!"
                    else -> "Perfect! We're going to be great friends."
                },
                isInteractionComplete = newCount >= 3
            )
        }
    }
}
