// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                                 = Json { allowStructuredMapKeys = true }
// val webHealthCareProviderProfileResponse = json.parse(WebHealthCareProviderProfileResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebHealthCareProviderProfileResponse (
    val clinic: String,
    val contact: String,
    val email: String,
    val firstname: String,
    val id: Long,
    val lastname: String
)
