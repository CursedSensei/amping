package com.pinghtdog.amping.ui

data class DashboardUiState(
    val firstname: String = "Leo",
    val profileType: ProfileType = ProfileType.KIDS,
    val isTodayTaken: Boolean = false,
    val currentStreak: Int = 4,
    val bestStreak: Int = 14,
    val heartQuota: Int = 3,
    val totalRegimenDays: Int = 30,
    val currentDay: Int = 22,
    val gracePeriodHours: Long = 48L,
    val isLoading: Boolean = false,
    val isNetworkMode: Boolean = false,
    val errorMessage: String? = null
)
