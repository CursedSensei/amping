package com.pinghtdog.amping.data.model

import kotlinx.serialization.Serializable

enum class SessionPhase {
    CONVERSATION,      // Phase 1: Conversation & check-up with Gabby
    SYMPTOM_LOGGING,   // Phase 2: Logging side-effects / checklist
    VDOT_CAPTURE,      // Phase 3: Secure VDOT camera recording
    VDOT_REVIEW,       // Phase 3 Review: Reviewing and uploading the recorded clip
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
