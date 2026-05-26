// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                    = Json { allowStructuredMapKeys = true }
// val authRefreshTokenPayload = json.parse(AuthRefreshTokenPayload.serializer(), jsonString)

package com.pinghtdog.amping.api_schemas

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class AuthRefreshTokenPayload (
    @SerialName("refresh_token")
    val refreshToken: String
)
