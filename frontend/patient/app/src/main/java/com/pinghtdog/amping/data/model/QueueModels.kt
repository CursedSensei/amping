package com.pinghtdog.amping.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QueueEntry(
    val id: String,
    val localEncryptedPath: String,
    val timestamp: Long,
    val profile: String,
    val retryCount: Int = 0,
    val status: String = "Pending", // "Pending", "Failed", "Uploading"
    val adherenceDayID: Long? = null
)
