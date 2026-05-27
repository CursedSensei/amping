// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                          = Json { allowStructuredMapKeys = true }
// val mobileWeeklyAdherenceResponse = json.parse(MobileWeeklyAdherenceResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class MobileWeeklyAdherenceResponse (
    @SerialName("adherence_days")
    val adherenceDays: List<MobileAdherenceDayEntry>,

    @SerialName("week_end")
    val weekEnd: String,

    @SerialName("week_start")
    val weekStart: String
)

@Serializable
data class MobileAdherenceDayEntry (
    val date: String,
    val status: AdherenceStatusEnum
)

@Serializable
enum class AdherenceStatusEnum(val value: String) {
    @SerialName("app_recorded") AppRecorded("app_recorded"),
    @SerialName("provider_reconciled") ProviderReconciled("provider_reconciled"),
    @SerialName("technical_miss") TechnicalMiss("technical_miss"),
    @SerialName("unverified_absence") UnverifiedAbsence("unverified_absence");
}
