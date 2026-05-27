package com.pinghtdog.amping.ui.onboarding
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. The State: Everything the UI needs to display
data class OnboardingUiState(
    val name: String = "",
    val selectedAgeGroup: AgeGroup? = null,
    val motivation: String = "",
    val showValidationError: Boolean = false
) {
    // Only allow the user to proceed if they entered a name, picked a group, and entered motivation
    val canProceed: Boolean
        get() = name.isNotBlank() && selectedAgeGroup != null && motivation.isNotBlank()
}

enum class AgeGroup(val displayName: String) {
    KID("Kid (Under 13)"),
    TEEN("Teen (13-17)"),
    ADULT("Adult (18-64)"),
    SENIOR("Senior (65+)")
}

// 2. The ViewModel: Handles the logic and updates the state
class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateName(newName: String) {
        _uiState.update { it.copy(name = newName, showValidationError = false) }
    }

    fun selectAgeGroup(group: AgeGroup) {
        _uiState.update { it.copy(selectedAgeGroup = group, showValidationError = false) }
    }

    fun updateMotivation(newMotivation: String) {
        _uiState.update { it.copy(motivation = newMotivation, showValidationError = false) }
    }

    fun onContinueClicked(context: android.content.Context, onSuccess: () -> Unit) {
        if (_uiState.value.canProceed) {
            try {
                val file = java.io.File(context.filesDir, "motivation.txt")
                file.writeText(_uiState.value.motivation)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onSuccess()
        } else {
            _uiState.update { it.copy(showValidationError = true) }
        }
    }
}