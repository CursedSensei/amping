// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                         = Json { allowStructuredMapKeys = true }
// val mobilePatientProfileResponse = json.parse(MobilePatientProfileResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class MobilePatientProfileResponse (
    val birthyear: Long,
    val contact: String,

    @SerialName("current_day")
    val currentDay: Long,

    val email: String,
    val firstname: String,
    val id: Long,
    val lastname: String,

    @SerialName("regimen_start")
    val regimenStart: String,

    @SerialName("total_days")
    val totalDays: Long
)
