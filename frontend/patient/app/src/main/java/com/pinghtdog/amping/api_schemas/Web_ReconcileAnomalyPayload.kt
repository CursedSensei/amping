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
    val verificationMethod: ReconciliationMethodEnum
)

@Serializable
enum class ReconciliationMethodEnum(val value: String) {
    @SerialName("dot_order") DotOrder("dot_order"),
    @SerialName("home_visit") HomeVisit("home_visit"),
    @SerialName("send_message") SendMessage("send_message");
}
