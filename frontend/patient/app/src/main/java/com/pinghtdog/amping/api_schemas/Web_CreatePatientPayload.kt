// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                    = Json { allowStructuredMapKeys = true }
// val webCreatePatientPayload = json.parse(WebCreatePatientPayload.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebCreatePatientPayload (
    val birthyear: Long,
    val contact: String,
    val email: String,
    val firstname: String,
    val guardians: List<WebPatientGuardianEntry>,
    val lastname: String,

    @SerialName("regimen_start")
    val regimenStart: String,

    @SerialName("total_days")
    val totalDays: Long
)

@Serializable
data class WebPatientGuardianEntry (
    val contact: String,
    val email: String,
    val firstname: String,
    val lastname: String
)
