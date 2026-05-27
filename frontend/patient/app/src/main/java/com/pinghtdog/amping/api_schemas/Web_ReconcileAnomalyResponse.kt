// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                        = Json { allowStructuredMapKeys = true }
// val webReconcileAnomalyResponse = json.parse(WebReconcileAnomalyResponse.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Returned after a successful reconciliation batch.
 */
@Serializable
data class WebReconcileAnomalyResponse (
    @SerialName("reconciled_count")
    val reconciledCount: Long,

    @SerialName("updated_heart_quota")
    val updatedHeartQuota: Long,

    @SerialName("updated_pdc")
    val updatedPdc: Double,

    @SerialName("updated_streak")
    val updatedStreak: Long
)
