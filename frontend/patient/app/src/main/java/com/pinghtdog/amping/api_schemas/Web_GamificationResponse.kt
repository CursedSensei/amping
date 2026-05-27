// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                    = Json { allowStructuredMapKeys = true }
// val webGamificationResponse = json.parse(WebGamificationResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebGamificationResponse (
    @SerialName("best_streak")
    val bestStreak: Long,

    @SerialName("current_streak")
    val currentStreak: Long,

    @SerialName("heart_quota")
    val heartQuota: Long,

    @SerialName("penalty_history")
    val penaltyHistory: List<WebPenaltyEvent>,

    @SerialName("total_regimen_days")
    val totalRegimenDays: Long
)

@Serializable
data class WebPenaltyEvent (
    val date: String,
    val label: String,
    val tier: Long
)
