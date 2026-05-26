// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                     = Json { allowStructuredMapKeys = true }
// val webPatientDetailResponse = json.parse(WebPatientDetailResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebPatientDetailResponse (
    val birthyear: Long,
    val contact: String,

    @SerialName("current_day")
    val currentDay: Long,

    val email: String,
    val firstname: String,
    val id: Long,
    val lastname: String,

    @SerialName("month3_protected")
    val month3Protected: Boolean,

    @SerialName("month_pdc")
    val monthPdc: Double,

    @SerialName("pdc_target")
    val pdcTarget: Double,

    @SerialName("regimen_start")
    val regimenStart: String,

    @SerialName("total_days")
    val totalDays: Long
)
