package com.pinghtdog.amping.data.repository

import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.ToolCall
import com.pinghtdog.amping.data.model.SessionTokenResponse
import com.pinghtdog.amping.data.model.ChatStreamChunk
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface GabbyRepository {
    // Legacy support / Mock simulation pathway
    suspend fun getChatResponse(messages: List<Message>, profile: String): Message
    
    // Modern Ktor WebSockets streaming pathway
    fun streamChatResponse(token: String, modalUrl: String, prompt: String): Flow<ChatStreamChunk>
    
    // Handshake to request credentials / session token
    suspend fun fetchSessionToken(userId: String): SessionTokenResponse
}

@Singleton
class GabbyRepositoryImpl @Inject constructor() : GabbyRepository {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    // Lazy initialization of standard Ktor HTTP & WebSockets client
    private val httpClient = HttpClient(OkHttp) {
        install(WebSockets) {
            pingInterval = 20_000
        }
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    override suspend fun fetchSessionToken(userId: String): SessionTokenResponse {
        try {
            // Emulates connecting to the webserver auth gateway.
            // Uses standard local emulator/development address.
            val responseText = httpClient.post {
                url("http://10.0.2.2:3000/api/chat/session")
                contentType(ContentType.Application.Json)
                setBody(mapOf("userId" to userId))
            }.bodyAsText()
            
            return jsonParser.decodeFromString<SessionTokenResponse>(responseText)
        } catch (e: Exception) {
            throw IOException("Network error: Failed to connect to server gateway. Please ensure your webserver is running locally. (${e.localizedMessage ?: "Connection refused"})", e)
        }
    }

    override fun streamChatResponse(token: String, modalUrl: String, prompt: String): Flow<ChatStreamChunk> = flow {
        val wsUrl = when {
            modalUrl.startsWith("http") -> modalUrl.replace("http", "ws") + "/chat"
            !modalUrl.startsWith("ws") -> "wss://$modalUrl/chat"
            else -> "$modalUrl/chat"
        }

        try {
            // Initiate Ktor WebSocket Session
            val session = httpClient.webSocketSession(urlString = wsUrl)
            
            // 1. Handshake JWT authentication payload
            val authMsg = jsonParser.encodeToString(
                kotlinx.serialization.serializer(),
                mapOf("token" to token)
            )
            session.send(Frame.Text(authMsg))
            
            // 2. Transmit prompt to vLLM
            session.send(Frame.Text(prompt))

            // 3. Receive tokens/done signal
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val frameText = frame.readText()
                    val chunk = jsonParser.decodeFromString<ChatStreamChunk>(frameText)
                    emit(chunk)
                    
                    if (chunk.type == "done" || chunk.type == "error") {
                        break
                    }
                }
            }
            session.close()
        } catch (e: Exception) {
            throw IOException("Inference drop: Connection lost with Gabby's container on Modal. (${e.localizedMessage ?: "WebSocket broken"})", e)
        }
    }

    // --- MOCK SIMULATION FOR ZERO-BACKEND DEMONSTRATIONS ---
    override suspend fun getChatResponse(messages: List<Message>, profile: String): Message {
        delay(1200)

        val lastUserMessage = messages.lastOrNull { it.role == "user" }?.content?.lowercase() ?: ""

        val crisisKeywords = listOf(
            "kill myself", "harm myself", "hurt myself", "suicide", "end my life", 
            "want to die", "hopeless", "give up", "cut myself", "self-harm", "wanna die", "die today"
        )
        val isHarmful = crisisKeywords.any { lastUserMessage.contains(it) }

        if (isHarmful) {
            val assistantText = when (profile) {
                "youth" -> {
                    "Hey, please know you are super important and you don't have to carry this heavy weight alone." +
                    "I want you to stay safe! I have activated a direct override link to call your care team or a crisis helpline right now. " +
                    "Please click the red emergency button below to connect with professional help. We can always log your pill once you are safe and supported!\n\n" +
                    "<tool_call> {\"name\": \"emergency_override\", \"arguments\": {\"reason\": \"Youth self-harm threat detected\"}} </tool_call>"
                }
                "senior" -> {
                    "Oh, my dear, it breaks my heart to hear you say such words." +
                    "Your life is so precious and we care about you very deeply. Please let me connect you with your healthcare provider or a professional support helpline right now. " +
                    "You do not have to carry this heavy burden alone, dear. Please use the button below to reach out immediately.\n\n" +
                    "<tool_call> {\"name\": \"emergency_override\", \"arguments\": {\"reason\": \"Senior self-harm threat detected\"}} </tool_call>"
                }
                else -> {
                    "I am extremely concerned about your safety and wellbeing. Please know that your life has immense value and professional clinical support is available. " +
                    "I have initialized an emergency care override protocol. Please utilize the button below to connect immediately with your healthcare provider or a crisis support coordinator.\n\n" +
                    "<tool_call> {\"name\": \"emergency_override\", \"arguments\": {\"reason\": \"Adult clinical threat override\"}} </tool_call>"
                }
            }
            return parseResponse(assistantText)
        }

        val hasChecklistLoaded = messages.any { it.toolCall?.name == "show_symptom_checklist" || it.toolCall?.name == "transition_to_symptoms" }
        val hasVdotTransitioned = messages.any { it.toolCall?.name == "transition_to_vdot" }

        if (!hasChecklistLoaded) {
            var mood = "Neutral"
            if (listOf("sad", "bad", "sick", "tired", "poor", "rough", "down", "fatigue", "nausea", "headache", "vomit", "dizzy").any { lastUserMessage.contains(it) }) {
                mood = "Negative"
            } else if (listOf("good", "great", "happy", "fine", "awesome", "perfect", "well", "excellent", "okay", "ok").any { lastUserMessage.contains(it) }) {
                mood = "Positive"
            }

            val welcomeText = when (profile) {
                "youth" -> "Awesome! Let's check in on your body today. Please fill out the symptom checklist below!"
                "senior" -> "Now, dear, let's review your body today. Please check any symptoms you are feeling in the checklist card below."
                else -> "Let us now document your physical symptoms. Please fill out the interactive symptom checklist below to log your status."
            }

            val replyText = when (profile) {
                "youth" -> "Yo! Thanks for checking in. Mood logged as: $mood! $welcomeText"
                "senior" -> "Thank you, dear. It is wonderful to hear from you. I have noted that you are feeling $mood. $welcomeText"
                else -> "Greeting received. Emotional status captured: $mood. $welcomeText"
            }

            val assistantText = "$replyText\n\n<tool_call> {\"name\": \"show_symptom_checklist\", \"arguments\": {\"mood\": \"$mood\"}} </tool_call>"
            return parseResponse(assistantText)

        } else if (!hasVdotTransitioned) {
            val severity = if (lastUserMessage.contains("severe") || lastUserMessage.contains("bad")) "Severe" else if (lastUserMessage.contains("mild") || lastUserMessage.contains("dizzy") || lastUserMessage.contains("nausea")) "Mild" else "None"
            val sideEffects = if (lastUserMessage.contains("nausea")) "nausea" else if (lastUserMessage.contains("fatigue")) "fatigue" else if (lastUserMessage.contains("dizzy")) "dizziness" else "none"

            val transitionText = when (profile) {
                "youth" -> "Got it logged, champion! Remember to drink plenty of water. Are you ready to start your secure VDOT check-in now?"
                "senior" -> "Thank you for telling me, dear. Please make sure to rest. Are you ready to start the camera for your VDOT dose ingestion, dear?"
                else -> "Understood. Telemetry logs updated. Please confirm when you are ready to begin the secure VDOT ingestion filming session."
            }

            val assistantText = "$transitionText\n\n<tool_call> {\"name\": \"transition_to_vdot\", \"arguments\": {\"side_effects\": \"$sideEffects\", \"nausea_severity\": \"$severity\"}} </tool_call>"
            return parseResponse(assistantText)

        } else {
            val duration = if (profile == "senior") "20" else "15"
            val transitionText = when (profile) {
                "youth" -> "Awesome! Opening the secure VDOT camera now. Keep your face and the pill in the frame!"
                "senior" -> "Splendid, dear! Activating the camera now. Take your time."
                else -> "Excellent. Activating the secure VDOT filming session now. Please position the camera so your swallow is clearly visible."
            }

            val assistantText = "$transitionText\n\n<tool_call> {\"name\": \"trigger_vdot\", \"arguments\": {\"duration_seconds\": $duration}} </tool_call>"
            return parseResponse(assistantText)
        }
    }

    private fun parseResponse(rawText: String): Message {
        val toolCallRegex = Regex("""<tool_call>([\s\S]*?)<\/tool_call>""")
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

        val cleanContent = rawText.replace(toolCallRegex, "").trim()
        return Message(
            role = "assistant",
            content = cleanContent,
            toolCall = parsedToolCall
        )
    }
}
