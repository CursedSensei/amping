// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                     = Json { allowStructuredMapKeys = true }
// val webCreatePatientResponse = json.parse(WebCreatePatientResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebCreatePatientResponse (
    val message: String
)
