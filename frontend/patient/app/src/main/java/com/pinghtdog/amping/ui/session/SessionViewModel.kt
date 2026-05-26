package com.pinghtdog.amping.ui.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.SessionPhase
import com.pinghtdog.amping.data.model.ToolCall
import com.pinghtdog.amping.data.repository.GabbyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class SessionViewModel @Inject constructor(
    private val gabbyRepository: GabbyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var fallbackJob: kotlinx.coroutines.Job? = null
    private var hasLoggedVoices = false

    init {
        initializeTts()
        initializeGreeting()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
                setupUtteranceListener()
            }
        }
    }

    private fun selectMaleVoice() {
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val currentLocale = tts?.language ?: Locale.getDefault()
                val currentLang = currentLocale.language.lowercase()

                if (!hasLoggedVoices) {
                    hasLoggedVoices = true
                    android.util.Log.i("GabbyTTS", "--- ALL AVAILABLE TTS VOICES ON THIS DEVICE ---")
                    voices.forEach { v ->
                        android.util.Log.i("GabbyTTS", "Voice Candidate - Name: ${v.name}, Locale: ${v.locale}, isNetworkRequired: ${v.isNetworkConnectionRequired}, features: ${v.features}")
                    }
                    android.util.Log.i("GabbyTTS", "---------------------------------------------")
                }

                fun languagesMatch(l1: Locale, l2: Locale): Boolean {
                    val lang1 = l1.language.lowercase()
                    val lang2 = l2.language.lowercase()
                    if (lang1 == lang2) return true
                    val clean1 = if (lang1 == "eng") "en" else if (lang1 == "spa") "es" else if (lang1 == "fra") "fr" else if (lang1 == "deu") "de" else lang1
                    val clean2 = if (lang2 == "eng") "en" else if (lang2 == "spa") "es" else if (lang2 == "fra") "fr" else if (lang2 == "deu") "de" else lang2
                    if (clean1 == clean2) return true
                    return lang1.startsWith(lang2) || lang2.startsWith(lang1)
                }

                // Search for a voice matching the active language and containing male identifiers
                val maleVoice = voices.find { voice ->
                    val nameLower = voice.name.lowercase()
                    val isLanguageMatch = languagesMatch(voice.locale, currentLocale)
                    val isMaleNamed = nameLower.contains("male") ||
                            nameLower.contains("guy") ||
                            nameLower.contains("man") ||
                            nameLower.contains("masculine") ||
                            nameLower.contains("iom") ||
                            nameLower.contains("sfg") ||
                            nameLower.contains("iob") ||
                            nameLower.contains("ioc") ||
                            nameLower.contains("iod") ||
                            nameLower.contains("iof") ||
                            nameLower.contains("ioh") ||
                            nameLower.contains("wavenet-b") ||
                            nameLower.contains("wavenet-d") ||
                            nameLower.contains("neural2-b") ||
                            nameLower.contains("neural2-d")

                    isLanguageMatch && isMaleNamed
                }

                if (maleVoice != null) {
                    val result = tts?.setVoice(maleVoice)
                    android.util.Log.d("GabbyTTS", "Selected male voice successfully: ${maleVoice.name}, result code: $result")
                } else {
                    android.util.Log.w("GabbyTTS", "No explicit male voice found matching active locale: ${currentLocale.language}. Fallback applied.")
                    // Fallback to any voice of the current language if a male voice is not explicitly labeled
                    val langVoice = voices.find { voice -> languagesMatch(voice.locale, currentLocale) }
                    if (langVoice != null) {
                        tts?.voice = langVoice
                        android.util.Log.d("GabbyTTS", "Applied language fallback voice: ${langVoice.name}")
                    }
                }
            } else {
                android.util.Log.w("GabbyTTS", "TTS voices list is null or empty inside selectMaleVoice.")
            }
        } catch (e: Exception) {
            android.util.Log.e("GabbyTTS", "Error during male voice selection: ${e.localizedMessage}", e)
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _uiState.update { it.copy(isTtsSpeaking = true) }
            }

            override fun onDone(utteranceId: String?) {
                _uiState.update { it.copy(isTtsSpeaking = false) }
                // Speech finished, safe to execute pending tool call transitions
                viewModelScope.launch {
                    executePendingToolCall()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _uiState.update { it.copy(isTtsSpeaking = false) }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _uiState.update { it.copy(isTtsSpeaking = false) }
            }
        })
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        
        // Extract content without XML-like tool tags
        val cleanText = text.replace(Regex("<tool_call>[\\s\\S]*?<\\/tool_call>"), "").trim()
        if (cleanText.isBlank()) return

        _uiState.update { it.copy(currentSubtitleText = cleanText) }

        val profile = _uiState.value.activeProfile
        val (pitch, rate) = when (profile) {
            "youth" -> Pair(1.15f, 1.05f)
            "senior" -> Pair(0.95f, 0.85f)
            else -> Pair(1.0f, 1.0f)
        }

        // Apply male voice lazy-style right before speaking when voices are guaranteed loaded
        selectMaleVoice()

        tts?.apply {
            setPitch(pitch)
            setSpeechRate(rate)
            
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gabby_utterance")
            }
            speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "gabby_utterance")
        }

        startFallbackTimer(cleanText)
    }

    private fun startFallbackTimer(text: String) {
        fallbackJob?.cancel()
        fallbackJob = viewModelScope.launch {
            // Words/min estimate: average 150 words/min -> 250ms per word. Use 400ms + padding for safety.
            val estimatedTimeMs = (text.split(" ").size * 400L).coerceAtLeast(3000L)
            delay(estimatedTimeMs)
            if (_uiState.value.pendingToolCallName != null) {
                executePendingToolCall()
            }
        }
    }

    fun executePendingToolCall() {
        val toolName = _uiState.value.pendingToolCallName ?: return
        
        fallbackJob?.cancel()
        
        _uiState.update { state ->
            var nextPhase = state.currentPhase
            when (toolName) {
                "show_symptom_checklist", "transition_to_symptoms" -> {
                    nextPhase = SessionPhase.SYMPTOM_LOGGING
                }
                "transition_to_vdot" -> {
                    nextPhase = SessionPhase.VDOT_CAPTURE
                }
                "trigger_vdot" -> {
                    nextPhase = SessionPhase.VDOT_CAPTURE
                }
            }
            state.copy(
                currentPhase = nextPhase,
                pendingToolCallName = null,
                pendingToolCallArgs = emptyMap()
            )
        }
    }

    private fun initializeGreeting() {
        viewModelScope.launch {
            _uiState.update { it.copy(assistantTyping = true) }
            delay(500)
            val welcomeMessage = when (_uiState.value.activeProfile) {
                "youth" -> "Hey there, Leo! ready for today's super check-in? How are you feeling overall, champion? Let me know so we can log your TB pill and keep that massive streak alive!"
                "senior" -> "Good day, Lola. It is time for our daily health check-in, my dear. How are you feeling overall today? Please tell me so we can complete your TB dose safely."
                else -> "Welcome to your daily VDOT compliance session. Please indicate your current overall state of physical wellbeing so we may proceed with logging your TB medication ingestion."
            }
            _uiState.update {
                it.copy(
                    chatHistory = listOf(Message(role = "assistant", content = welcomeMessage)),
                    assistantTyping = false
                )
            }
            speak(welcomeMessage)
        }
    }

    fun selectProfile(profile: String) {
        if (_uiState.value.activeProfile == profile) return
        _uiState.value = SessionUiState(activeProfile = profile)
        initializeGreeting()
    }

    fun toggleNetworkMode(enabled: Boolean) {
        _uiState.update { it.copy(isNetworkMode = enabled) }
    }

    fun dismissNetworkError() {
        _uiState.update { it.copy(networkError = null) }
    }

    fun sendMessage(content: String) {
        val currentMessages = _uiState.value.chatHistory.toMutableList()
        val userMessage = Message(role = "user", content = content)
        currentMessages.add(userMessage)

        // Cancel pending timers/speeches when patient speaks
        fallbackJob?.cancel()
        tts?.stop()

        _uiState.update {
            it.copy(
                chatHistory = currentMessages,
                assistantTyping = true,
                networkError = null, // Clear any active error card
                currentSubtitleText = "",
                isTtsSpeaking = false,
                pendingToolCallName = null,
                pendingToolCallArgs = emptyMap()
            )
        }

        viewModelScope.launch {
            if (_uiState.value.isNetworkMode) {
                runNetworkChatFlow(content)
            } else {
                runMockChatFlow(currentMessages)
            }
        }
    }

    private suspend fun runMockChatFlow(currentMessages: List<Message>) {
        try {
            val response = gabbyRepository.getChatResponse(currentMessages, _uiState.value.activeProfile)
            handleInferenceResult(response)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    assistantTyping = false,
                    chatHistory = it.chatHistory + Message(role = "assistant", content = "Sorry, I had trouble connecting. Let's try that check-in again.")
                )
            }
        }
    }

    private suspend fun runNetworkChatFlow(prompt: String) {
        try {
            // 1. Fetch transient Session JWT Token
            val sessionInfo = gabbyRepository.fetchSessionToken(_uiState.value.activeProfile)

            // Inject temporary streaming bubble
            val initialHistory = _uiState.value.chatHistory.toMutableList()
            val streamMessageIndex = initialHistory.size
            initialHistory.add(Message(role = "assistant", content = ""))
            _uiState.update {
                it.copy(
                    chatHistory = initialHistory,
                    assistantTyping = false
                )
            }

            var streamingContent = ""

            // 2. Stream tokens via Ktor WebSockets
            gabbyRepository.streamChatResponse(
                token = sessionInfo.token,
                modalUrl = sessionInfo.modalUrl,
                prompt = prompt
            ).collect { chunk ->
                when (chunk.type) {
                    "token" -> {
                        chunk.content?.let { token ->
                            streamingContent += token
                            _uiState.update { state ->
                                val updatedHistory = state.chatHistory.toMutableList()
                                if (streamMessageIndex < updatedHistory.size) {
                                    updatedHistory[streamMessageIndex] = Message(role = "assistant", content = streamingContent)
                                }
                                state.copy(chatHistory = updatedHistory)
                            }
                        }
                    }
                    "error" -> {
                        throw Exception(chunk.message ?: "Modal inference container reported an internal error.")
                    }
                    "done" -> {
                        // Finished streaming. Parse the final accumulated string for tool-call matches
                        val finalMessageText = _uiState.value.chatHistory.getOrNull(streamMessageIndex)?.content ?: streamingContent
                        val parsedResponse = parseResponse(finalMessageText)
                        handleInferenceResult(parsedResponse)
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessage = e.localizedMessage ?: "Failed to reach Gabby."
            _uiState.update {
                it.copy(
                    assistantTyping = false,
                    networkError = errorMessage,
                    chatHistory = it.chatHistory + Message(role = "assistant", content = "⚠️ Network Connection Interrupted: $errorMessage")
                )
            }
        }
    }

    private fun handleInferenceResult(response: Message) {
        val updatedMessages = _uiState.value.chatHistory.toMutableList()
        val existingIndex = updatedMessages.indexOfFirst { 
            it.role == "assistant" && (it.content == response.content || it.content.replace("<tool_call>", "").trim() == response.content) 
        }

        if (existingIndex != -1) {
            updatedMessages[existingIndex] = response
        } else {
            updatedMessages.add(response)
        }

        _uiState.update { state ->
            var nextPhase = state.currentPhase
            var emergencyReason: String? = state.emergencyState
            var pendingName: String? = null
            var pendingArgs: Map<String, String> = emptyMap()

            // Intercept and act upon tool calling emitted from Gabby
            response.toolCall?.let { tool ->
                when (tool.name) {
                    "emergency_override" -> {
                        emergencyReason = tool.arguments["reason"] ?: "Self-harm override activated"
                        nextPhase = SessionPhase.CONVERSATION
                    }
                    "show_symptom_checklist", "transition_to_symptoms", "transition_to_vdot", "trigger_vdot" -> {
                        // Defer transitions to allow user to read/hear TTS response first
                        pendingName = tool.name
                        pendingArgs = tool.arguments
                    }
                }
            }

            state.copy(
                chatHistory = updatedMessages,
                assistantTyping = false,
                currentPhase = nextPhase,
                emergencyState = emergencyReason,
                pendingToolCallName = pendingName,
                pendingToolCallArgs = pendingArgs
            )
        }

        speak(response.content)
        updateQuickReplies()
    }

    private fun parseResponse(rawText: String): Message {
        val toolCallRegex = Regex("""<tool_call>([\s\S]*?)<\/tool_call>""")
        val match = toolCallRegex.find(rawText)
        var parsedToolCall: ToolCall? = null

        if (match != null) {
            val jsonStr = match.groupValues[1].trim()
            try {
                parsedToolCall = Json { ignoreUnknownKeys = true }.decodeFromString<ToolCall>(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
                // Robust Regex-based Fallback Parser for LLM-generated malformed JSON
                try {
                    val nameMatch = Regex(""""name"\s*:\s*["']([^"']+)["']""").find(jsonStr)
                    val name = nameMatch?.groupValues?.get(1)
                    if (name != null) {
                        val argsMap = mutableMapOf<String, String>()
                        val argsMatch = Regex("""["']([^"']+)["']\s*:\s*["']([^"']+)["']""").findAll(jsonStr)
                        for (arg in argsMatch) {
                            val key = arg.groupValues[1]
                            val value = arg.groupValues[2]
                            if (key != "name" && key != "arguments") {
                                argsMap[key] = value
                            }
                        }
                        parsedToolCall = ToolCall(name = name, arguments = argsMap)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        val cleanContent = rawText.replace(toolCallRegex, "").trim()
        return Message(
            role = "assistant",
            content = cleanContent,
            toolCall = parsedToolCall
        )
    }


    fun startSpeechListening() {
        if (_uiState.value.isListening) return
        
        // Stop any active TTS speaking to avoid recording Gabby's own voice!
        tts?.stop()
        _uiState.update { it.copy(isTtsSpeaking = false) }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            // Fallback to simulation if speech recognizer is not supported on this device/emulator
            simulateSpeechInput()
            return
        }

        viewModelScope.launch {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _uiState.update { it.copy(isListening = true, currentSubtitleText = "Listening...") }
                        }

                        override fun onBeginningOfSpeech() {
                            _uiState.update { it.copy(currentSubtitleText = "Hearing your voice...") }
                        }

                        override fun onRmsChanged(rmsdB: Float) {}

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _uiState.update { it.copy(currentSubtitleText = "Processing speech...") }
                        }

                        override fun onError(error: Int) {
                            _uiState.update { it.copy(isListening = false) }
                            val errorMsg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found. Try again!"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech engine is busy"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Try again!"
                                else -> "Speech recognition failed"
                            }
                            _uiState.update { it.copy(currentSubtitleText = errorMsg) }
                        }

                        override fun onResults(results: Bundle?) {
                            _uiState.update { it.copy(isListening = false) }
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()
                            if (!text.isNullOrBlank()) {
                                sendMessage(text)
                            } else {
                                _uiState.update { it.copy(currentSubtitleText = "Could not hear you clearly. Please try again!") }
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()
                            if (!text.isNullOrBlank()) {
                                _uiState.update { it.copy(currentSubtitleText = text) }
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            
            speechRecognizer?.startListening(intent)
        }
    }

    fun stopSpeechListening() {
        speechRecognizer?.stopListening()
        _uiState.update { it.copy(isListening = false) }
    }

    fun simulateSpeechInput() {
        if (_uiState.value.isListening) return
        viewModelScope.launch {
            _uiState.update { it.copy(isListening = true, currentSubtitleText = "Listening (Simulated dictation)...") }
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

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        fallbackJob?.cancel()
        super.onCleared()
    }
}
