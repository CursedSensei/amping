// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                = Json { allowStructuredMapKeys = true }
// val webPDCTrendResponse = json.parse(WebPDCTrendResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WebPDCTrendResponse (
    @SerialName("pdc_target")
    val pdcTarget: Double,

    @SerialName("weekly_pdc")
    val weeklyPdc: List<WebWeeklyPDCEntry>
)

@Serializable
data class WebWeeklyPDCEntry (
    val pdc: Double,
    val week: Long
)
