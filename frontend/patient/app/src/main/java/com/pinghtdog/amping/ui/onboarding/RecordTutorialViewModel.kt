package com.pinghtdog.amping.ui.onboarding

import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Videocam
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecordStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class RecordTutorialUiState(
    val currentStepIndex: Int = 0,
    val steps: List<RecordStep> = listOf(
        RecordStep(
            title = "Talk to Gabby",
            description = "Gabby will guide you through the process.",
            icon = androidx.compose.material.icons.Icons.Default.ChatBubble
        ),
        RecordStep(
            title = "Camera Activates",
            description = "The camera will start recording automatically.",
            icon = androidx.compose.material.icons.Icons.Default.Videocam
        ),
        RecordStep(
            title = "Show Medicine",
            description = "Hold your medicine up to the camera clearly.",
            icon = androidx.compose.material.icons.Icons.Default.MedicalServices
        ),
        RecordStep(
            title = "Show Empty Mouth",
            description = "Finally, show that you've swallowed the dose.",
            icon = androidx.compose.material.icons.Icons.Default.Face
        )
    )
) {
    val currentStep: RecordStep = steps[currentStepIndex]
    val isLastStep: Boolean = currentStepIndex == steps.size - 1
}

class RecordTutorialViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecordTutorialUiState())
    val uiState: StateFlow<RecordTutorialUiState> = _uiState.asStateFlow()

    fun nextStep() {
        if (!_uiState.value.isLastStep) {
            _uiState.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStepIndex > 0) {
            _uiState.update { it.copy(currentStepIndex = it.currentStepIndex - 1) }
        }
    }
}
