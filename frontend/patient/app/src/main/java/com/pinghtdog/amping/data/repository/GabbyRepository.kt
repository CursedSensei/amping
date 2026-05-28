package com.pinghtdog.amping.data.repository

import android.content.Context
import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.ToolCall
import com.pinghtdog.amping.data.model.SessionTokenResponse
import com.pinghtdog.amping.data.model.ChatStreamChunk
import com.pinghtdog.amping.api_schemas.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.request.header
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
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
    suspend fun fetchSessionToken(userId: String, motivation: String? = null): SessionTokenResponse

    // Upload video bytes to backend
    suspend fun uploadVideo(videoBytes: ByteArray): String

    // --- PRODUCTION MOBILE API ENDPOINTS ---
    suspend fun refreshAccessToken(context: Context): String
    suspend fun getPatientProfile(context: Context): MobilePatientProfileResponse
    suspend fun getHealthcareProfile(context: Context): MobileHealthCareProviderProfileResponse
    suspend fun getStats(context: Context): MobileStatsResponse
    suspend fun getWeeklyAdherence(context: Context): MobileWeeklyAdherenceResponse
    suspend fun uploadSymptoms(context: Context, date: String, symptoms: List<String>): MobileUploadSymtomsResponse
    suspend fun getAdherenceVideoEndpoint(context: Context): MobileGetAdherenceVideoEndpointResponse
    suspend fun uploadVideoToProduction(context: Context, videoBytes: ByteArray): String
}

@Singleton
class GabbyRepositoryImpl @Inject constructor() : GabbyRepository {

    companion object {
        // Toggle between Render production and local development server running on host port 8000
        private const val BASE_URL = "http://10.0.2.2:8000"
        // private const val BASE_URL = "https://amping.onrender.com"
    }

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

    private suspend fun getValidAccessToken(context: Context): String {
        val cached = TokenManager.getAccessToken(context)
        if (cached != null) return cached
        return refreshAccessToken(context)
    }

    private suspend inline fun <reified T> executeAuthenticatedRequest(
        context: Context,
        crossinline block: suspend HttpClient.(accessToken: String) -> HttpResponse
    ): T {
        var token = getValidAccessToken(context)
        var response = try {
            httpClient.block(token)
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 401) {
                // Token might be expired, refresh it and retry
                token = refreshAccessToken(context)
                httpClient.block(token)
            } else {
                throw e
            }
        }
        
        if (response.status.value == 401) {
            token = refreshAccessToken(context)
            response = httpClient.block(token)
        }
        
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IOException("API Error (${response.status.value}): $text")
        }
        
        return jsonParser.decodeFromString(text)
    }

    override suspend fun refreshAccessToken(context: Context): String {
        val refreshToken = TokenManager.getRefreshToken(context)
            ?: throw IOException("No refresh token stored on this device. Please seed refresh_token.txt.")
        
        try {
            val responseText = httpClient.post {
                url("$BASE_URL/api/v1/mobile/refresh-token/")
                contentType(ContentType.Application.Json)
                setBody(MobileRefreshTokenPayload(refreshToken))
            }.bodyAsText()
            
            val payload = jsonParser.decodeFromString<MobileRefreshTokenResponse>(responseText)
            TokenManager.saveAccessToken(context, payload.accessToken)
            return payload.accessToken
        } catch (e: Exception) {
            throw IOException("Token rotation failed: Failed to exchange refresh token for an access token with backend. (${e.localizedMessage ?: "Network error"})", e)
        }
    }

    override suspend fun getPatientProfile(context: Context): MobilePatientProfileResponse {
        return executeAuthenticatedRequest(context) { token ->
            get("$BASE_URL/api/v1/mobile/profile/") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun getHealthcareProfile(context: Context): MobileHealthCareProviderProfileResponse {
        return executeAuthenticatedRequest(context) { token ->
            get("$BASE_URL/api/v1/mobile/healthcare-profile/") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun getStats(context: Context): MobileStatsResponse {
        return executeAuthenticatedRequest(context) { token ->
            get("$BASE_URL/api/v1/mobile/stats/") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun getWeeklyAdherence(context: Context): MobileWeeklyAdherenceResponse {
        return executeAuthenticatedRequest(context) { token ->
            get("$BASE_URL/api/v1/mobile/weekly_adherence/") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun uploadSymptoms(context: Context, date: String, symptoms: List<String>): MobileUploadSymtomsResponse {
        return executeAuthenticatedRequest(context) { token ->
            post("$BASE_URL/api/v1/mobile/upload_symptoms/") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(MobileUploadSymtomsPayload(date = date, symptoms = symptoms))
            }
        }
    }

    override suspend fun getAdherenceVideoEndpoint(context: Context): MobileGetAdherenceVideoEndpointResponse {
        return executeAuthenticatedRequest(context) { token ->
            get("$BASE_URL/api/v1/mobile/adherence_video_endpoint/") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun uploadVideoToProduction(context: Context, videoBytes: ByteArray): String {
        // 1. Handshake to retrieve signed upload video endpoint URL
        val endpointResponse = getAdherenceVideoEndpoint(context)
        val videoEndpoint = endpointResponse.videoEndpoint

        // 2. Perform multipart form binary stream upload to this resolved production endpoint
        var token = getValidAccessToken(context)
        var response = try {
            httpClient.submitFormWithBinaryData(
                url = videoEndpoint,
                formData = formData {
                    append("video", videoBytes, Headers.build {
                        append(HttpHeaders.ContentType, "video/mp4")
                        append(HttpHeaders.ContentDisposition, "filename=\"vdot.mp4\"")
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 401) {
                token = refreshAccessToken(context)
                httpClient.submitFormWithBinaryData(
                    url = videoEndpoint,
                    formData = formData {
                        append("video", videoBytes, Headers.build {
                            append(HttpHeaders.ContentType, "video/mp4")
                            append(HttpHeaders.ContentDisposition, "filename=\"vdot.mp4\"")
                        })
                    }
                ) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            } else {
                throw e
            }
        }

        if (response.status.value == 401) {
            token = refreshAccessToken(context)
            response = httpClient.submitFormWithBinaryData(
                url = videoEndpoint,
                formData = formData {
                    append("video", videoBytes, Headers.build {
                        append(HttpHeaders.ContentType, "video/mp4")
                        append(HttpHeaders.ContentDisposition, "filename=\"vdot.mp4\"")
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IOException("Failed to upload production video: (${response.status.value}) $text")
        }

        return text
    }

    override suspend fun fetchSessionToken(userId: String, motivation: String?): SessionTokenResponse {
        try {
            val responseText = httpClient.post {
                url("$BASE_URL/api/chat/session/")
                contentType(ContentType.Application.Json)
                setBody(mapOf("userId" to userId, "motivation" to motivation))
            }.bodyAsText()
            
            return jsonParser.decodeFromString<SessionTokenResponse>(responseText)
        } catch (e: Exception) {
            throw IOException("Network error: Failed to connect to server gateway at https://amping.onrender.com. (${e.localizedMessage ?: "Connection refused"})", e)
        }
    }

    // Keep legacy support for uploadVideo byte streaming (local emulator fallback)
    override suspend fun uploadVideo(videoBytes: ByteArray): String {
        try {
            val responseText = httpClient.post {
                url("http://10.0.2.2:3000/api/upload_video")
                contentType(ContentType.Application.OctetStream)
                setBody(videoBytes)
            }.bodyAsText()
            return responseText
        } catch (e: Exception) {
            throw IOException("Failed to upload VDOT video to local server: ${e.localizedMessage}", e)
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

        val isUploadComplete = lastUserMessage.contains("vdot upload complete") || lastUserMessage.contains("upload complete")
        if (isUploadComplete) {
            val motivationStr = if (lastUserMessage.contains("motivation:")) {
                messages.lastOrNull { it.role == "user" }?.content?.substringAfter("Motivation:")?.trim() ?: ""
            } else ""
            val motivationText = if (motivationStr.isNotEmpty()) " because of '$motivationStr'" else ""
            val transitionText = when (profile) {
                "youth" -> "Awesome job, champion! You successfully completed today's ingestion and uploaded the VDOT video. Remember the reason why you are taking this medication$motivationText! Keep that streak alive!"
                "senior" -> "Splendid work, Lola. You have successfully completed your daily medication check-in and video upload. Remember the reason why you are taking this medication$motivationText. Your health is so precious, dear."
                else -> "Ingestion verification video uploaded successfully. Remember the reason why you are taking this medication$motivationText. Compliance log updated."
            }
            val assistantText = "$transitionText\n\n<tool_call> {\"name\": \"transition_to_success\"} </tool_call>"
            return parseResponse(assistantText)
        }

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
            val isConfirmed = listOf("yes", "start", "ready", "confirm", "ok", "sure", "now", "begin", "go", "video", "camera", "ingest", "button", "pill").any { lastUserMessage.contains(it) } &&
                    !listOf("not yet", "no", "wait", "hold", "stop", "cancel", "later").any { lastUserMessage.contains(it) }

            val assistantText = if (isConfirmed) {
                val transitionText = when (profile) {
                    "youth" -> "Awesome! Opening the secure VDOT camera now. Keep your face and the pill in the frame!"
                    "senior" -> "Splendid, dear! Activating the camera now. Take your time."
                    else -> "Excellent. Activating the secure VDOT filming session now. Please position the camera so your swallow is clearly visible."
                }
                "$transitionText\n\n<tool_call> {\"name\": \"trigger_vdot\", \"arguments\": {\"duration_seconds\": $duration}} </tool_call>"
            } else {
                val standByText = when (profile) {
                    "youth" -> "No worries, buddy! Take your time. Just say the word or tap when you're ready to show me that pill!"
                    "senior" -> "Of course, Lola. Do not rush. Let me know when you feel prepared to take your medication."
                    else -> "Understood. Remaining on standby. Please confirm when you are ready to proceed with the secure VDOT ingestion."
                }
                standByText
            }
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
