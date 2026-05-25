// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                = Json { allowStructuredMapKeys = true }
// val refreshTokenPayload = json.parse(RefreshTokenPayload.serializer(), jsonString)

package quicktype

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class RefreshTokenPayload (
    @SerialName("refresh_token")
    val refreshToken: String
)
