package com.pinghtdog.amping.data.model

import kotlinx.serialization.Serializable

enum class SessionPhase {
    CONVERSATION,      // Phase 1: Conversation & check-up with Gabby
    SYMPTOM_LOGGING,   // Phase 2: Logging side-effects / checklist
    VDOT_CAPTURE,      // Phase 3: Secure VDOT camera recording
    VDOT_REVIEW,       // Phase 3 Review: Reviewing and uploading the recorded clip
    VDOT_SYNCING,      // Cryptographic encryption & transmission sync screen
    VDOT_QUEUE,        // Offline Video Queue & Manual Retry Screen
    SUCCESS            // Complete! XP, Rewards, and Streaks
}

@Serializable
data class ToolCall(
    val name: String,
    val arguments: Map<String, String> = emptyMap()
)

@Serializable
data class Message(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val toolCall: ToolCall? = null
)

@Serializable
data class SessionTokenResponse(
    val token: String,
    val modalUrl: String
)

@Serializable
data class ChatStreamChunk(
    val type: String, // "token", "done", "error"
    val content: String? = null,
    val message: String? = null
)
