// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                              = Json { allowStructuredMapKeys = true }
// val mobileAdherenceVideoStatusPayload = json.parse(MobileAdherenceVideoStatusPayload.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class MobileAdherenceVideoStatusPayload (
    @SerialName("adherence_day_id")
    val adherenceDayID: Long,

    val status: AdherenceVideoStatusEnum
)

@Serializable
enum class AdherenceVideoStatusEnum(val value: String) {
    @SerialName("failed") Failed("failed"),
    @SerialName("success") Success("success");
}
