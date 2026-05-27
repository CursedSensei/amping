package com.pinghtdog.amping.ui.onboarding

import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PermissionType {
    CAMERA,
    MICROPHONE,
    NOTIFICATIONS
}

data class PermissionStep(
    val type: PermissionType,
    val title: String,
    val description: String,
    val reassurance: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class PermissionsUiState(
    val currentStepIndex: Int = 0,
    val permissions: List<PermissionStep> = listOf(
        PermissionStep(
            type = PermissionType.CAMERA,
            title = "Camera Access",
            description = "We use the camera to record your dose videos.",
            reassurance = "We will never record without you starting.",
            icon = androidx.compose.material.icons.Icons.Default.CameraAlt
        ),
        PermissionStep(
            type = PermissionType.MICROPHONE,
            title = "Microphone Access",
            description = "This allows you to talk to Gabby while recording.",
            reassurance = "Audio is only captured during active sessions.",
            icon = androidx.compose.material.icons.Icons.Default.Mic
        ),
        PermissionStep(
            type = PermissionType.NOTIFICATIONS,
            title = "Notifications",
            description = "Stay on track with daily reminders from Gabby.",
            reassurance = "You can change this anytime in settings.",
            icon = androidx.compose.material.icons.Icons.Default.Notifications
        )
    )
) {
    val currentStep: PermissionStep? = permissions.getOrNull(currentStepIndex)
    val isLastStep: Boolean = currentStepIndex == permissions.size - 1
}

class PermissionsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean, onAllComplete: () -> Unit) {
        // In a real app, we'd handle the actual permission request here or in the UI layer
        if (_uiState.value.isLastStep) {
            onAllComplete()
        } else {
            _uiState.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
        }
    }
}
