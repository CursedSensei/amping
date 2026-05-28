package com.pinghtdog.amping.ui

data class DashboardUiState(
    val firstname: String = "Leo",
    val currentStreak: Int = 4,
    val bestStreak: Int = 14,
    val heartQuota: Int = 3,
    val totalRegimenDays: Int = 400,
    val currentDay: Int = 30,
    val isLoading: Boolean = false,
    val isNetworkMode: Boolean = false,
    val errorMessage: String? = null
)
