package com.pinghtdog.amping.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinghtdog.amping.data.repository.GabbyRepository
import com.pinghtdog.amping.data.repository.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val gabbyRepository: GabbyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Automatically check if a refresh token exists to determine network mode feasibility
        val hasToken = TokenManager.getRefreshToken(context) != null
        _uiState.update { it.copy(isNetworkMode = hasToken) }
        loadDashboardData()
    }

    fun toggleNetworkMode(enabled: Boolean) {
        _uiState.update { it.copy(isNetworkMode = enabled) }
        loadDashboardData()
    }

    fun loadDashboardData() {
        if (!_uiState.value.isNetworkMode) {
            // Revert to default simulated mock values
            _uiState.update { 
                DashboardUiState(isNetworkMode = false)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Fetch dynamic patient profile details from local/render server
                val profile = gabbyRepository.getPatientProfile(context)
                val stats = gabbyRepository.getStats(context)

                _uiState.update { state ->
                    state.copy(
                        firstname = profile.firstname,
                        currentStreak = stats.currentStreak.toInt(),
                        bestStreak = stats.bestStreak.toInt(),
                        heartQuota = stats.heartQuota.toInt(),
                        totalRegimenDays = stats.totalRegimenDays.toInt(),
                        currentDay = profile.currentDay.toInt(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Failed to load dashboard data from network", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Offline or server error. Switched to offline metrics."
                    ) 
                }
            }
        }
    }
}
