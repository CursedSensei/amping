package com.pinghtdog.amping.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoading: Boolean = true,
    val version: String = "v1.0.0-beta"
)

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun startInitialization(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Simulate asset pre-load and token validation
            delay(2500) 
            _uiState.update { it.copy(isLoading = false) }
            onComplete()
        }
    }
}
