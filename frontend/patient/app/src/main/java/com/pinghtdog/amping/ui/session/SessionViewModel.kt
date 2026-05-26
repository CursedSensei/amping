package com.pinghtdog.amping.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.SessionPhase
import com.pinghtdog.amping.data.repository.GabbyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val gabbyRepository: GabbyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        initializeGreeting()
    }

    private fun initializeGreeting() {
        viewModelScope.launch {
            _uiState.update { it.copy(assistantTyping = true) }
            delay(500)
            val welcomeMessage = when (_uiState.value.activeProfile) {
                "youth" -> "Hey there, Leo! ready for today's super check-in? 🚀 How are you feeling overall, champion? Let me know so we can log your TB pill and keep that massive streak alive!"
                "senior" -> "Good day, Lola. ❤️ It is time for our daily health check-in, my dear. How are you feeling overall today? Please tell me so we can complete your TB dose safely."
                else -> "Welcome to your daily VDOT compliance session. Please indicate your current overall state of physical wellbeing so we may proceed with logging your TB medication ingestion."
            }
            _uiState.update {
                it.copy(
                    chatHistory = listOf(Message(role = "assistant", content = welcomeMessage)),
                    assistantTyping = false
                )
            }
        }
    }

    fun selectProfile(profile: String) {
        if (_uiState.value.activeProfile == profile) return
        _uiState.value = SessionUiState(activeProfile = profile)
        initializeGreeting()
    }

    fun sendMessage(content: String) {
        val currentMessages = _uiState.value.chatHistory.toMutableList()
        val userMessage = Message(role = "user", content = content)
        currentMessages.add(userMessage)

        _uiState.update {
            it.copy(
                chatHistory = currentMessages,
                assistantTyping = true
            )
        }

        viewModelScope.launch {
            try {
                val response = gabbyRepository.getChatResponse(currentMessages, _uiState.value.activeProfile)
                val updatedMessages = _uiState.value.chatHistory.toMutableList()
                updatedMessages.add(response)

                _uiState.update { state ->
                    var nextPhase = state.currentPhase
                    var emergencyReason: String? = state.emergencyState

                    // Intercept and act upon tool calling emitted from Gabby
                    response.toolCall?.let { tool ->
                        when (tool.name) {
                            "emergency_override" -> {
                                emergencyReason = tool.arguments["reason"] ?: "Self-harm override activated"
                            }
                            "show_symptom_checklist" -> {
                                nextPhase = SessionPhase.SYMPTOM_LOGGING
                            }
                            "transition_to_vdot" -> {
                                nextPhase = SessionPhase.VDOT_CAPTURE
                            }
                            "trigger_vdot" -> {
                                nextPhase = SessionPhase.VDOT_CAPTURE
                            }
                        }
                    }

                    state.copy(
                        chatHistory = updatedMessages,
                        assistantTyping = false,
                        currentPhase = nextPhase,
                        emergencyState = emergencyReason
                    )
                }

                updateQuickReplies()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        assistantTyping = false,
                        chatHistory = it.chatHistory + Message(role = "assistant", content = "Sorry, I had trouble connecting. Let's try that check-in again.")
                    )
                }
            }
        }
    }

    fun simulateSpeechInput() {
        if (_uiState.value.isListening) return
        viewModelScope.launch {
            _uiState.update { it.copy(isListening = true) }
            delay(2000) // Simulating listening/recording
            val mockTranscriptions = when (_uiState.value.activeProfile) {
                "youth" -> "I am feeling pretty awesome, ready to record!"
                "senior" -> "Hello Gabby, my body is feeling quite good today, dear."
                else -> "Doing fine today, ready to complete the medication check."
            }
            _uiState.update { it.copy(isListening = false) }
            sendMessage(mockTranscriptions)
        }
    }

    fun selectSymptom(symptom: String, checked: Boolean) {
        val currentSelected = _uiState.value.selectedSymptoms.toMutableSet()
        if (checked) {
            currentSelected.add(symptom)
        } else {
            currentSelected.remove(symptom)
        }
        _uiState.update { state ->
            state.copy(
                selectedSymptoms = currentSelected,
                nauseaSeverity = if (!currentSelected.contains("Nausea")) "None" else state.nauseaSeverity
            )
        }
    }

    fun selectNauseaSeverity(severity: String) {
        _uiState.update { it.copy(nauseaSeverity = severity) }
    }

    fun submitSymptoms() {
        val symptoms = _uiState.value.selectedSymptoms
        val severity = _uiState.value.nauseaSeverity

        val symptomString = if (symptoms.isEmpty() || symptoms.contains("None of these")) {
            "no side effects"
        } else {
            symptoms.joinToString(", ") + (if (symptoms.contains("Nausea")) " ($severity severity)" else "")
        }

        // Send a simulated user action to Gabby
        val messageContent = "Symptoms reported: $symptomString."
        sendMessage(messageContent)
    }

    fun proceedToVideoRecording() {
        // Direct transition when confirmed
        sendMessage("Ready to start VDOT filming.")
    }

    fun startCameraRecording() {
        _uiState.update { it.copy(currentPhase = SessionPhase.VDOT_CAPTURE) }
    }

    fun completeRecording() {
        _uiState.update { it.copy(currentPhase = SessionPhase.VDOT_REVIEW) }
    }

    fun uploadVideo() {
        if (_uiState.value.isUploading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = 0f) }
            for (i in 1..10) {
                delay(250)
                _uiState.update { it.copy(uploadProgress = i * 0.1f) }
            }
            _uiState.update {
                it.copy(
                    isUploading = false,
                    currentPhase = SessionPhase.SUCCESS,
                    streakCount = it.streakCount + 1
                )
            }
        }
    }

    fun resetSession() {
        _uiState.value = SessionUiState(activeProfile = _uiState.value.activeProfile)
        initializeGreeting()
    }

    // --- DEVELOPER DEBUG PANEL OVERRIDES (OPTION B) ---

    fun forcePhase(phase: SessionPhase) {
        _uiState.update { state ->
            val chatHistory = state.chatHistory.toMutableList()
            if (chatHistory.isEmpty()) {
                chatHistory.add(Message(role = "assistant", content = "Debug State Force initialized."))
            }
            state.copy(
                currentPhase = phase,
                emergencyState = if (phase != SessionPhase.CONVERSATION) null else state.emergencyState
            )
        }
    }

    fun triggerEmergencyOverride(reason: String) {
        _uiState.update {
            it.copy(
                emergencyState = reason,
                currentPhase = SessionPhase.CONVERSATION
            )
        }
    }

    private fun updateQuickReplies() {
        val phase = _uiState.value.currentPhase
        val replies = when (phase) {
            SessionPhase.CONVERSATION -> {
                if (_uiState.value.activeProfile == "youth") {
                    listOf("Feelin' epic! 😎", "A bit tired today 🥱", "My stomach hurts 🤢")
                } else if (_uiState.value.activeProfile == "senior") {
                    listOf("I feel quite well, dear.", "Feeling a bit weak today.", "Struggling with nausea.")
                } else {
                    listOf("Feeling fully healthy.", "Experiencing minor fatigue.", "Experiencing mild nausea.")
                }
            }
            else -> emptyList()
        }
        _uiState.update { it.copy(quickReplies = replies) }
    }
}
