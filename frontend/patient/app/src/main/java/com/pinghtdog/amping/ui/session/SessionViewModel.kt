package com.pinghtdog.amping.ui.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.SessionPhase
import com.pinghtdog.amping.data.model.ToolCall
import com.pinghtdog.amping.data.repository.GabbyRepository
import com.pinghtdog.amping.data.repository.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class SessionViewModel @Inject constructor(
    private val gabbyRepository: GabbyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        // Shared Json instance — creating a new one per parseResponse() call is slow.
        private val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
    }

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var fallbackJob: kotlinx.coroutines.Job? = null
    private var hasLoggedVoices = false

    init {
        val hasToken = TokenManager.getRefreshToken(context) != null
        _uiState.update { it.copy(isNetworkMode = hasToken) }
        initializeTts()
        loadOfflineQueue()
        startPeriodicBackgroundSync()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
                setupUtteranceListener()
                initializeGreeting()
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
            var nextStage = state.conversationStage
            when (toolName) {
                "show_symptom_checklist", "transition_to_symptoms" -> {
                    nextPhase = SessionPhase.SYMPTOM_LOGGING
                    nextStage = 2
                }
                "transition_to_vdot" -> {
                    nextPhase = SessionPhase.CONVERSATION
                    nextStage = 3
                }
                "trigger_vdot" -> {
                    nextPhase = SessionPhase.VDOT_CAPTURE
                }
                "transition_to_success" -> {
                    nextPhase = SessionPhase.SUCCESS
                }
            }
            state.copy(
                currentPhase = nextPhase,
                conversationStage = nextStage,
                pendingToolCallName = null,
                pendingToolCallArgs = emptyMap()
            )
        }
    }

    private fun initializeGreeting() {
        viewModelScope.launch {
            _uiState.update { it.copy(assistantTyping = true) }
            
            if (_uiState.value.isNetworkMode) {
                try {
                    val profile = gabbyRepository.getPatientProfile(context)
                    val stats = gabbyRepository.getStats(context)
                    _uiState.update { it.copy(
                        firstname = profile.firstname,
                        streakCount = stats.currentStreak.toInt()
                    ) }
                } catch (e: Exception) {
                    android.util.Log.e("SessionViewModel", "Failed to pre-fetch patient data for greeting", e)
                }
            } else {
                val mockName = when (_uiState.value.activeProfile) {
                    "youth" -> "Leo"
                    "senior" -> "Lola"
                    else -> "Patient"
                }
                _uiState.update { it.copy(firstname = mockName) }
            }
            
            delay(500)
            val name = _uiState.value.firstname
            val welcomeMessage = when (_uiState.value.activeProfile) {
                "youth" -> "Hello $name! Ready for today? How are you feeling overall, champion? Let me know so we can keep that streak alive!"
                "senior" -> "Good day, $name. It is time for our daily health check-in, my dear. How are you feeling today?"
                else -> "Welcome, $name, to your daily VDOT compliance session. Please indicate your current overall state of physical wellbeing."
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
        val currentNetworkMode = _uiState.value.isNetworkMode
        _uiState.value = SessionUiState(activeProfile = profile, isNetworkMode = currentNetworkMode)
        initializeGreeting()
    }

    fun toggleNetworkMode(enabled: Boolean) {
        _uiState.update { it.copy(isNetworkMode = enabled) }
        if (enabled) {
            fetchProductionStats()
        }
    }

    fun fetchProductionStats() {
        viewModelScope.launch {
            try {
                val profile = gabbyRepository.getPatientProfile(context)
                val stats = gabbyRepository.getStats(context)
                _uiState.update { it.copy(
                    firstname = profile.firstname,
                    streakCount = stats.currentStreak.toInt()
                ) }
            } catch (e: Exception) {
                android.util.Log.e("SessionViewModel", "Failed to fetch production stats", e)
            }
        }
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
            val errorMessage = e.localizedMessage ?: "Failed to reach Gabby."
            android.util.Log.e("GabbyMock", "Error running mock chat flow", e)
            _uiState.update {
                it.copy(
                    assistantTyping = false,
                    networkError = errorMessage,
                    chatHistory = it.chatHistory + Message(role = "assistant", content = "Sorry, I had trouble connecting. Let's try that check-in again.")
                )
            }
        }
    }

    private suspend fun runNetworkChatFlow(prompt: String) {
        var streamMessageIndex = -1
        try {
            // Read local motivation text file
            val motivation = try {
                val file = java.io.File(context.filesDir, "motivation.txt")
                if (file.exists()) file.readText().trim() else ""
            } catch (e: Exception) {
                ""
            }

            val phaseStr = when (_uiState.value.conversationStage) {
                2 -> "symptoms"
                3 -> "vdot"
                else -> "empathy"
            }

            // 1. Fetch transient Session JWT Token
            val sessionInfo = gabbyRepository.fetchSessionToken(
                userId = _uiState.value.activeProfile,
                motivation = if (motivation.isNotEmpty()) motivation else null,
                currentPhase = phaseStr
            )

            // Inject temporary streaming bubble with helpful cold start message
            val initialHistory = _uiState.value.chatHistory.toMutableList()
            streamMessageIndex = initialHistory.size
            val historyToSend = initialHistory.takeLast(10)
            val sleepingMsg = "💤 Gabby is sleeping... (This may take 3-5 minutes, please keep the app open)..."
            initialHistory.add(Message(role = "assistant", content = sleepingMsg))
            _uiState.update {
                it.copy(
                    chatHistory = initialHistory,
                    assistantTyping = false,
                    currentSubtitleText = sleepingMsg
                )
            }

            var streamingContent = ""
            var isFirstToken = true

            // 2. Stream tokens via Ktor WebSockets, passing full conversation history
            gabbyRepository.streamChatResponse(
                token = sessionInfo.token,
                modalUrl = sessionInfo.modalUrl,
                messages = historyToSend
            ).collect { chunk ->
                when (chunk.type) {
                    "connected" -> {
                        _uiState.update { state ->
                            val updatedHistory = state.chatHistory.toMutableList()
                            if (streamMessageIndex != -1 && streamMessageIndex < updatedHistory.size) {
                                updatedHistory[streamMessageIndex] = Message(role = "assistant", content = "🤔 Gabby is thinking...")
                            }
                            state.copy(
                                chatHistory = updatedHistory,
                                currentSubtitleText = "🤔 Gabby is thinking..."
                            )
                        }
                    }
                    "token" -> {
                        chunk.content?.let { token ->
                            if (isFirstToken) {
                                isFirstToken = false
                                streamingContent = ""
                                _uiState.update { it.copy(currentSubtitleText = "") }
                            }
                            streamingContent += token
                            
                            val displayContent = if (streamingContent.contains("<think>")) {
                                if (streamingContent.contains("</think>")) {
                                    streamingContent.substringAfter("</think>").trim()
                                } else {
                                    "🤔 Gabby is thinking..."
                                }
                            } else {
                                streamingContent
                            }
                            
                            _uiState.update { state ->
                                val updatedHistory = state.chatHistory.toMutableList()
                                if (streamMessageIndex != -1 && streamMessageIndex < updatedHistory.size) {
                                    val cleanDisplay = displayContent.replace(Regex("<tool_call>[\\s\\S]*?<\\/tool_call>"), "").trim()
                                    updatedHistory[streamMessageIndex] = Message(role = "assistant", content = cleanDisplay)
                                }
                                state.copy(chatHistory = updatedHistory)
                            }
                        }
                    }
                    "error" -> {
                        throw Exception(chunk.message ?: "Modal inference container reported an internal error.")
                    }
                    "done" -> {
                        // Always parse from the raw accumulated content — the chat history copy
                        // has already had <tool_call> tags stripped for display purposes, so
                        // reading from chatHistory here would silently discard the tool call.
                        val parsedResponse = parseResponse(streamingContent)
                        handleInferenceResult(parsedResponse)
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessage = e.localizedMessage ?: "Failed to reach Gabby."
            android.util.Log.e("GabbyNetwork", "Error running network chat flow: $errorMessage", e)
            _uiState.update { state ->
                val updatedHistory = state.chatHistory.toMutableList()
                if (streamMessageIndex != -1 && streamMessageIndex < updatedHistory.size) {
                    updatedHistory[streamMessageIndex] = Message(role = "assistant", content = "⚠️ Network Connection Interrupted: $errorMessage")
                } else {
                    updatedHistory.add(Message(role = "assistant", content = "⚠️ Network Connection Interrupted: $errorMessage"))
                }
                state.copy(
                    assistantTyping = false,
                    networkError = errorMessage,
                    chatHistory = updatedHistory
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
            var nextStage = state.conversationStage
            var nextRepromptCount = state.vdotRepromptCount

            // Intercept and act upon tool calling emitted from Gabby
            response.toolCall?.let { tool ->
                when (tool.name) {
                    "emergency_override" -> {
                        emergencyReason = tool.arguments["reason"] ?: "Self-harm override activated"
                        nextPhase = SessionPhase.CONVERSATION
                    }
                    "show_symptom_checklist", "transition_to_symptoms" -> {
                        // Defer transitions to allow user to read/hear TTS response first
                        pendingName = tool.name
                        pendingArgs = tool.arguments
                        nextStage = 2
                    }
                    "transition_to_vdot" -> {
                        pendingName = tool.name
                        pendingArgs = tool.arguments
                        nextStage = 3
                    }
                    "trigger_vdot", "transition_to_success" -> {
                        pendingName = tool.name
                        pendingArgs = tool.arguments
                    }
                }
            }

            // Increment re-prompt count if in Stage 3 and we didn't receive a trigger_vdot tool call
            if (state.conversationStage == 3 && response.toolCall?.name != "trigger_vdot") {
                nextRepromptCount++
            }

            state.copy(
                chatHistory = updatedMessages,
                assistantTyping = false,
                currentPhase = nextPhase,
                conversationStage = nextStage,
                vdotRepromptCount = nextRepromptCount,
                emergencyState = emergencyReason,
                pendingToolCallName = pendingName,
                pendingToolCallArgs = pendingArgs
            )
        }

        speak(response.content)
        updateQuickReplies()
    }

    internal fun parseResponse(rawText: String): Message {
        val toolCallRegex = Regex("""<tool_call>([\s\S]*?)<\/tool_call>""")
        val thinkRegex = Regex("""<think>([\s\S]*?)<\/think>""")

        // Log raw response and any extracted thinking block to Android Logcat
        android.util.Log.d("LLM_Output", "--- RAW LLM RESPONSE RECEIVED ---")
        android.util.Log.d("LLM_Output", rawText)

        val thinkMatch = thinkRegex.find(rawText)
        if (thinkMatch != null) {
            val thinkingBlock = thinkMatch.groupValues[1].trim()
            android.util.Log.d("LLM_Output", "--- EXTRACTED THINKING BLOCK ---")
            android.util.Log.d("LLM_Output", thinkingBlock)
        }

        val match = toolCallRegex.find(rawText)
        var parsedToolCall: ToolCall? = null

        if (match != null) {
            val jsonStr = match.groupValues[1].trim()
            try {
                parsedToolCall = jsonParser.decodeFromString<ToolCall>(jsonStr)
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

        // Clean out both think tags and tool call tags from final displayed content
        val contentWithoutThink = rawText.replace(thinkRegex, "").trim()
        val cleanContent = contentWithoutThink.replace(toolCallRegex, "").trim()

        android.util.Log.d("LLM_Output", "--- FINAL CLEANED RESPONSE CONTENT ---")
        android.util.Log.d("LLM_Output", cleanContent)
        android.util.Log.d("LLM_Output", "--------------------------------------")

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

        val symptomList = if (symptoms.isEmpty() || symptoms.contains("None of these")) {
            emptyList()
        } else {
            symptoms.map { if (it == "Nausea") "nausea ($severity severity)" else it.lowercase() }
        }

        val symptomString = if (symptomList.isEmpty()) {
            "no side effects"
        } else {
            symptomList.joinToString(", ")
        }

        // Send a simulated user action to Gabby
        val messageContent = "Symptoms reported: $symptomString."
        
        if (_uiState.value.isNetworkMode) {
            viewModelScope.launch {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val dateStr = sdf.format(java.util.Date())
                    val response = gabbyRepository.uploadSymptoms(context, dateStr, symptomList)
                    _uiState.update { it.copy(adherenceDayID = response.adherenceDayID) }
                } catch (e: Exception) {
                    android.util.Log.e("SessionViewModel", "Failed to upload symptoms to production", e)
                }
            }
        }
        
        sendMessage(messageContent)
    }

    fun proceedToVideoRecording() {
        // Direct transition when confirmed
        sendMessage("Ready to start VDOT filming.")
    }

    fun startCameraRecording() {
        _uiState.update { it.copy(currentPhase = SessionPhase.VDOT_CAPTURE) }
    }

    fun completeRecording(filePath: String) {
        _uiState.update { 
            it.copy(
                recordedVideoPath = filePath,
                currentPhase = SessionPhase.VDOT_REVIEW
            )
        }
    }

    fun uploadVideo() {
        if (_uiState.value.isUploading) return
        val videoPath = _uiState.value.recordedVideoPath ?: return
        val isNetworkMode = _uiState.value.isNetworkMode

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    currentPhase = SessionPhase.VDOT_SYNCING,
                    isUploading = true,
                    uploadProgressState = 0.0f,
                    syncStatusText = "Encrypting VDOT payload..."
                ) 
            }

            try {
                val originalBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    if (videoPath.startsWith("content://")) {
                        context.contentResolver.openInputStream(android.net.Uri.parse(videoPath))?.use { it.readBytes() } ?: ByteArray(0)
                    } else {
                        java.io.File(videoPath).readBytes()
                    }
                }
                
                _uiState.update { it.copy(uploadProgressState = 0.2f, syncStatusText = "Securing sandbox storage...") }
                delay(300)
                
                val encryptedBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    VideoEncryptor.encrypt(originalBytes)
                }

                val encryptedVideosDir = java.io.File(context.filesDir, "encrypted_videos")
                if (!encryptedVideosDir.exists()) {
                    encryptedVideosDir.mkdirs()
                }
                
                val encryptedFile = java.io.File(encryptedVideosDir, "vdot_${System.currentTimeMillis()}.enc")
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    encryptedFile.writeBytes(encryptedBytes)
                }
                
                _uiState.update { it.copy(uploadProgressState = 0.4f, syncStatusText = "Registering payload in offline queue...") }
                delay(300)

                val queueEntry = com.pinghtdog.amping.data.repository.OfflineQueueManager.addEntry(
                    context,
                    encryptedFile.absolutePath,
                    _uiState.value.activeProfile,
                    adherenceDayID = _uiState.value.adherenceDayID
                )
                loadOfflineQueue()

                val isConnected = isNetworkAvailable()
                if (!isConnected && isNetworkMode) {
                    com.pinghtdog.amping.data.repository.OfflineQueueManager.updateEntryStatus(context, queueEntry.id, "Failed")
                    loadOfflineQueue()
                    
                    _uiState.update { 
                        it.copy(
                            isUploading = false,
                            currentPhase = SessionPhase.VDOT_QUEUE
                        ) 
                    }
                    return@launch
                }

                _uiState.update { it.copy(uploadProgressState = 0.6f, syncStatusText = "Opening secure transmission tunnel...") }
                delay(300)
                
                if (isNetworkMode) {
                    _uiState.update { it.copy(uploadProgressState = 0.8f, syncStatusText = "Uploading encrypted blocks via HTTPS...") }
                    
                    com.pinghtdog.amping.data.repository.OfflineQueueManager.updateEntryStatus(context, queueEntry.id, "Uploading")
                    loadOfflineQueue()
                    
                    val adherenceDayID = _uiState.value.adherenceDayID
                    gabbyRepository.uploadVideoToProduction(context, encryptedBytes, adherenceDayID)
                    
                    com.pinghtdog.amping.data.repository.OfflineQueueManager.removeEntry(context, queueEntry.id)
                    loadOfflineQueue()
                } else {
                    for (i in 6..9) {
                        delay(200)
                        _uiState.update { it.copy(uploadProgressState = i * 0.1f, syncStatusText = "Simulating HTTPS transfer...") }
                    }
                    com.pinghtdog.amping.data.repository.OfflineQueueManager.removeEntry(context, queueEntry.id)
                    loadOfflineQueue()
                }

                _uiState.update { it.copy(uploadProgressState = 1.0f, syncStatusText = "Server verification complete!") }
                delay(300)

                _uiState.update {
                    it.copy(
                        isUploading = false,
                        currentPhase = SessionPhase.CONVERSATION,
                        streakCount = it.streakCount + 1
                    )
                }

                val motivation = try {
                    val file = java.io.File(context.filesDir, "motivation.txt")
                    if (file.exists()) file.readText().trim() else ""
                } catch (e: Exception) {
                    ""
                }

                val uploadPrompt = if (motivation.isNotEmpty()) {
                    "VDOT upload complete. Motivation: $motivation"
                } else {
                    "VDOT upload complete."
                }

                sendMessage(uploadPrompt)

            } catch (e: Exception) {
                android.util.Log.e("SessionViewModel", "Upload failed", e)
                
                val latestQueue = com.pinghtdog.amping.data.repository.OfflineQueueManager.getQueue(context)
                val lastEntry = latestQueue.lastOrNull()
                if (lastEntry != null) {
                    com.pinghtdog.amping.data.repository.OfflineQueueManager.updateEntryStatus(
                        context, 
                        lastEntry.id, 
                        "Failed", 
                        lastEntry.retryCount + 1
                    )
                    loadOfflineQueue()
                }

                _uiState.update {
                    it.copy(
                        isUploading = false,
                        currentPhase = SessionPhase.VDOT_QUEUE
                    )
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: SecurityException) {
            android.util.Log.e("SessionViewModel", "SecurityException checking network state (ACCESS_NETWORK_STATE permission missing or not granted)", e)
            false
        } catch (e: Exception) {
            android.util.Log.e("SessionViewModel", "Error checking network state", e)
            false
        }
    }

    fun loadOfflineQueue() {
        val queue = com.pinghtdog.amping.data.repository.OfflineQueueManager.getQueue(context)
        _uiState.update { it.copy(offlineQueue = queue) }
    }

    private fun startPeriodicBackgroundSync() {
        viewModelScope.launch {
            while (true) {
                delay(15000)
                if (isNetworkAvailable() && _uiState.value.isNetworkMode) {
                    syncOfflineQueue()
                }
            }
        }
    }

    fun syncOfflineQueue() {
        val queue = com.pinghtdog.amping.data.repository.OfflineQueueManager.getQueue(context)
        if (queue.isEmpty()) return
        
        viewModelScope.launch {
            queue.forEach { entry ->
                if (!isNetworkAvailable()) return@launch
                
                try {
                    com.pinghtdog.amping.data.repository.OfflineQueueManager.updateEntryStatus(context, entry.id, "Uploading")
                    loadOfflineQueue()
                    
                    val file = java.io.File(entry.localEncryptedPath)
                    if (file.exists()) {
                        val encryptedBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            file.readBytes()
                        }
                        
                        val adherenceDayID = entry.adherenceDayID ?: _uiState.value.adherenceDayID
                        gabbyRepository.uploadVideoToProduction(context, encryptedBytes, adherenceDayID)
                        
                        com.pinghtdog.amping.data.repository.OfflineQueueManager.removeEntry(context, entry.id)
                        loadOfflineQueue()
                        
                        if (_uiState.value.currentPhase == SessionPhase.VDOT_QUEUE) {
                            _uiState.update {
                                it.copy(
                                    currentPhase = SessionPhase.CONVERSATION,
                                    streakCount = it.streakCount + 1
                                )
                            }
                            
                            val motivation = try {
                                val motFile = java.io.File(context.filesDir, "motivation.txt")
                                if (motFile.exists()) motFile.readText().trim() else ""
                            } catch (e: Exception) {
                                ""
                            }
                            
                            val uploadPrompt = if (motivation.isNotEmpty()) {
                                "VDOT upload complete. Motivation: $motivation"
                            } else {
                                "VDOT upload complete."
                            }
                            sendMessage(uploadPrompt)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SessionViewModel", "Background sync failed for entry ${entry.id}", e)
                    com.pinghtdog.amping.data.repository.OfflineQueueManager.updateEntryStatus(
                        context, 
                        entry.id, 
                        "Failed", 
                        entry.retryCount + 1
                    )
                    loadOfflineQueue()
                }
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
                conversationStage = when (phase) {
                    SessionPhase.CONVERSATION -> 1
                    SessionPhase.SYMPTOM_LOGGING -> 2
                    else -> 3
                },
                vdotRepromptCount = 0,
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

    fun bypassToolCallToVideoRecording() {
        _uiState.update { it.copy(currentPhase = SessionPhase.VDOT_CAPTURE) }
    }

    private fun updateQuickReplies() {
        val phase = _uiState.value.currentPhase
        val replies = when (phase) {
            SessionPhase.CONVERSATION -> {
                if (_uiState.value.conversationStage == 3) {
                    if (_uiState.value.activeProfile == "youth") {
                        listOf("Ready", "Not yet", "Wait")
                    } else if (_uiState.value.activeProfile == "senior") {
                        listOf("Yes, ready", "Not yet", "Wait a bit")
                    } else {
                        listOf("Yes, ready", "Not yet", "Wait")
                    }
                } else {
                    if (_uiState.value.activeProfile == "youth") {
                        listOf("Epic! 😎", "Tired 🥱", "Sick 🤢")
                    } else if (_uiState.value.activeProfile == "senior") {
                        listOf("Feeling well", "Feeling weak", "Nauseous")
                    } else {
                        listOf("Healthy", "Tired", "Nauseous")
                    }
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
