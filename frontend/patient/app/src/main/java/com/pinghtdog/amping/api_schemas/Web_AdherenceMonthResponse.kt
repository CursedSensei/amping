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
    val year: Long
)

@Serializable
data class WebAdherenceDayEntry (
    @SerialName("adherence_type")
    val adherenceType: String,

    val date: String,
    val symptoms: List<String>,

    @SerialName("video_link")
    val videoLink: String? = null
)
