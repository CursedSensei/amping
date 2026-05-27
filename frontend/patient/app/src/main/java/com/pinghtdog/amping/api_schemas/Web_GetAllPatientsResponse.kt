// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                      = Json { allowStructuredMapKeys = true }
// val webGetAllPatientsResponse = json.parse(WebGetAllPatientsResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebGetAllPatientsResponse (
    val patients: List<WebPatientEntry>
)

@Serializable
data class WebPatientEntry (
    val birthyear: Long,
    val contact: String,
    val email: String,
    val firstname: String,
    val id: Long,
    val lastname: String
)
