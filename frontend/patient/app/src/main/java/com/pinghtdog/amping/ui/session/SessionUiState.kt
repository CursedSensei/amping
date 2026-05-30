package com.pinghtdog.amping.ui.session

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
    val firstname: String = "Leo",
    val streakCount: Int = 5,
    val xpEarned: Int = 120,
    val isNetworkMode: Boolean = false, // Toggle Ktor vs Mock mode
    val networkError: String? = null, // Non-null displays network communication error card
    
    // Voice-guided UI specific states
    val currentSubtitleText: String = "",
    val isTtsSpeaking: Boolean = false,
    val pendingToolCallName: String? = null,
    val pendingToolCallArgs: Map<String, String> = emptyMap(),
    val recordedVideoPath: String? = null,
    val adherenceDayID: Long? = null,
    
    // Conversational stage tracking
    val conversationStage: Int = 1, // 1 = greeting, 2 = symptoms logging, 3 = VDOT ingestion confirmation
    val vdotRepromptCount: Int = 0,
    
    // Cryptographic Queueing and Sync states
    val offlineQueue: List<com.pinghtdog.amping.data.model.QueueEntry> = emptyList(),
    val syncStatusText: String = "",
    val uploadProgressState: Float = 0f
)
