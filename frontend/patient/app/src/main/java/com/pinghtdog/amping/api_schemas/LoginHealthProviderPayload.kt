// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                       = Json { allowStructuredMapKeys = true }
// val loginHealthProviderPayload = json.parse(LoginHealthProviderPayload.serializer(), jsonString)

package quicktype

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class LoginHealthProviderPayload (
    val email: String,
    val password: String
)
