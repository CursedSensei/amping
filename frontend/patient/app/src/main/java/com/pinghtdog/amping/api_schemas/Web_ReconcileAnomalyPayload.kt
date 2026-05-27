// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                       = Json { allowStructuredMapKeys = true }
// val webReconcileAnomalyPayload = json.parse(WebReconcileAnomalyPayload.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Sent by a healthcare provider or BHW to reconcile one or more anomalous
 * entries (e.g. mark a technical-miss as provider-verified).
 */
@Serializable
data class WebReconcileAnomalyPayload (
    @SerialName("entry_ids")
    val entryIDS: List<Long>,

    val reason: String,

    @SerialName("verification_method")
    val verificationMethod: AdherenceStatusEnum
)

@Serializable
enum class AdherenceStatusEnum(val value: String) {
    @SerialName("app_recorded") AppRecorded("app_recorded"),
    @SerialName("provider_reconciled") ProviderReconciled("provider_reconciled"),
    @SerialName("technical_miss") TechnicalMiss("technical_miss"),
    @SerialName("unverified_absence") UnverifiedAbsence("unverified_absence");
}
