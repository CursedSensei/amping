// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                        = Json { allowStructuredMapKeys = true }
// val webAnomalousEntriesResponse = json.parse(WebAnomalousEntriesResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebAnomalousEntriesResponse (
    val entries: List<WebAnomalousEntry>
)

@Serializable
data class WebAnomalousEntry (
    val date: String,
    val reason: String
)
