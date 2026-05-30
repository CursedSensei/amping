// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                = Json { allowStructuredMapKeys = true }
// val mobileStatsResponse = json.parse(MobileStatsResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class MobileStatsResponse (
    @SerialName("best_streak")
    val bestStreak: Long,

    @SerialName("current_streak")
    val currentStreak: Long,

    @SerialName("grace_period_hours")
    val gracePeriodHours: Long,

    @SerialName("heart_quota")
    val heartQuota: Long,

    @SerialName("penalty_history")
    val penaltyHistory: List<MobilePenaltyEvent>,

    @SerialName("total_regimen_days")
    val totalRegimenDays: Long
)

@Serializable
data class MobilePenaltyEvent (
    val date: String,
    val label: String,
    val tier: Long
)
