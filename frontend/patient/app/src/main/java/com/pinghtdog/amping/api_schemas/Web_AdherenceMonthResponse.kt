// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                      = Json { allowStructuredMapKeys = true }
// val webAdherenceMonthResponse = json.parse(WebAdherenceMonthResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebAdherenceMonthResponse (
    @SerialName("adherence_days")
    val adherenceDays: List<WebAdherenceDayEntry>,

    val month: Long,

    @SerialName("month_pdc")
    val monthPdc: Double,

    @SerialName("pdc_target")
    val pdcTarget: Double,

    val year: Long
)

@Serializable
data class WebAdherenceDayEntry (
    val date: String,
    val id: Long,
    val status: AdherenceStatusEnum,
    val symptoms: List<String>,

    @SerialName("video_link")
    val videoLink: String? = null
)

@Serializable
enum class AdherenceStatusEnum(val value: String) {
    @SerialName("app_recorded") AppRecorded("app_recorded"),
    @SerialName("provider_reconciled") ProviderReconciled("provider_reconciled"),
    @SerialName("technical_miss") TechnicalMiss("technical_miss"),
    @SerialName("unverified_absence") UnverifiedAbsence("unverified_absence");
}
