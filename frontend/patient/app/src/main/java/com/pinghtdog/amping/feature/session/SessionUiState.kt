package com.pinghtdog.amping.feature.session

import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.SessionPhase

data class SessionUiState(
    val currentPhase: SessionPhase = SessionPhase.CONVERSATION,
    val chatHistory: List<Message> = emptyList(),
    val selectedSymptoms: Set<String> = emptySet(),
    val nauseaSeverity: String = "None",
    val isListening: Boolean = false, // Voice input animation state
    val quickReplies: List<String> = listOf("Great! Ready.", "Okay, let's do it.", "Feeling tired/sick today..."),
    val assistantTyping: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val emergencyState: String? = null, // Non-null displays crisis override card
    val activeProfile: String = "youth", // "youth", "senior", "adult"
    val streakCount: Int = 5,
    val xpEarned: Int = 120
)
