package com.pinghtdog.amping.data.repository

import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.ToolCall
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface GabbyRepository {
    suspend fun getChatResponse(messages: List<Message>, profile: String): Message
}

@Singleton
class GabbyRepositoryImpl @Inject constructor() : GabbyRepository {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    override suspend fun getChatResponse(messages: List<Message>, profile: String): Message {
        // Simulate remote network delay
        delay(1200)

        val lastUserMessage = messages.lastOrNull { it.role == "user" }?.content?.lowercase() ?: ""

        // 1. CLINICAL CRISIS & SELF-HARM OVERRIDE
        val crisisKeywords = listOf(
            "kill myself", "harm myself", "hurt myself", "suicide", "end my life", 
            "want to die", "hopeless", "give up", "cut myself", "self-harm", "wanna die", "die today"
        )
        val isHarmful = crisisKeywords.any { lastUserMessage.contains(it) }

        if (isHarmful) {
            val assistantText = when (profile) {
                "youth" -> {
                    "Hey, please know you are super important and you don't have to carry this heavy weight alone. 🤝 " +
                    "I want you to stay safe! I have activated a direct override link to call your care team or a crisis helpline right now. " +
                    "Please click the red emergency button below to connect with professional help. We can always log your pill once you are safe and supported! ❤️\n\n" +
                    "<tool_call> {\"name\": \"emergency_override\", \"arguments\": {\"reason\": \"Youth self-harm threat detected\"}} </tool_call>"
                }
                "senior" -> {
                    "Oh, my dear, it breaks my heart to hear you say such words. ❤️ " +
                    "Your life is so precious and we care about you very deeply. Please let me connect you with your healthcare provider or a professional support helpline right now. " +
                    "You do not have to carry this heavy burden alone, dear. Please use the button below to reach out immediately.\n\n" +
                    "<tool_call> {\"name\": \"emergency_override\", \"arguments\": {\"reason\": \"Senior self-harm threat detected\"}} </tool_call>"
                }
                else -> { // adult
                    "I am extremely concerned about your safety and wellbeing. Please know that your life has immense value and professional clinical support is available. " +
                    "I have initialized an emergency care override protocol. Please utilize the button below to connect immediately with your healthcare provider or a crisis support coordinator.\n\n" +
                    "<tool_call> {\"name\": \"emergency_override\", \"arguments\": {\"reason\": \"Adult clinical threat override\"}} </tool_call>"
                }
            }
            return parseResponse(assistantText)
        }

        // 2. DETECT TRANSITIONS AND EMIT MOCK LLM TELEMETRY
        // Let's check what phase the conversation history indicates
        val hasChecklistLoaded = messages.any { it.content.contains("<tool_call>") && it.content.contains("show_symptom_checklist") }
        val hasVdotTransitioned = messages.any { it.content.contains("<tool_call>") && it.content.contains("transition_to_vdot") }

        if (!hasChecklistLoaded) {
            // PHASE 1 CONVERSATION: Determine mood from input
            var mood = "Neutral"
            if (listOf("sad", "bad", "sick", "tired", "poor", "rough", "down", "fatigue", "nausea", "headache", "vomit", "dizzy").any { lastUserMessage.contains(it) }) {
                mood = "Negative"
            } else if (listOf("good", "great", "happy", "fine", "awesome", "perfect", "well", "excellent", "okay", "ok").any { lastUserMessage.contains(it) }) {
                mood = "Positive"
            }

            val welcomeText = when (profile) {
                "youth" -> "Awesome! Let's check in on your body today. Please fill out the symptom checklist below! 🎬"
                "senior" -> "Now, dear, let's review your body today. Please check any symptoms you are feeling in the checklist card below."
                else -> "Let us now document your physical symptoms. Please fill out the interactive symptom checklist below to log your status."
            }

            val replyText = when (profile) {
                "youth" -> "Yo! Thanks for checking in. Mood logged as: $mood! 🚀 $welcomeText"
                "senior" -> "Thank you, dear. It is wonderful to hear from you. I have noted that you are feeling $mood. $welcomeText"
                else -> "Greeting received. Emotional status captured: $mood. $welcomeText"
            }

            val assistantText = "$replyText\n\n<tool_call> {\"name\": \"show_symptom_checklist\", \"arguments\": {\"mood\": \"$mood\"}} </tool_call>"
            return parseResponse(assistantText)

        } else if (!hasVdotTransitioned) {
            // PHASE 2 SYMPTOMS SUBMITTED: Parse symptoms logged in the simulation
            // In our mock, this response is triggered when the patient confirms checklist submission.
            val severity = if (lastUserMessage.contains("severe") || lastUserMessage.contains("bad")) "Severe" else if (lastUserMessage.contains("mild") || lastUserMessage.contains("dizzy") || lastUserMessage.contains("nausea")) "Mild" else "None"
            val sideEffects = if (lastUserMessage.contains("nausea")) "nausea" else if (lastUserMessage.contains("fatigue")) "fatigue" else if (lastUserMessage.contains("dizzy")) "dizziness" else "none"

            val transitionText = when (profile) {
                "youth" -> "Got it logged, champion! Remember to drink plenty of water. Are you ready to start your secure VDOT check-in now? 🚀"
                "senior" -> "Thank you for telling me, dear. Please make sure to rest. Are you ready to start the camera for your VDOT dose ingestion, dear? ❤️"
                else -> "Understood. Telemetry logs updated. Please confirm when you are ready to begin the secure VDOT ingestion filming session."
            }

            val assistantText = "$transitionText\n\n<tool_call> {\"name\": \"transition_to_vdot\", \"arguments\": {\"side_effects\": \"$sideEffects\", \"nausea_severity\": \"$severity\"}} </tool_call>"
            return parseResponse(assistantText)

        } else {
            // PHASE 3 VDOT CONFIRMED
            val duration = if (profile == "senior") "20" else "15"
            val transitionText = when (profile) {
                "youth" -> "Awesome! Opening the secure VDOT camera now. Keep your face and the pill in the frame! 🎬"
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
            }
        }

        // Clean tool_call tags from content for clean UI rendering
        val cleanContent = rawText.replace(toolCallRegex, "").trim()
        return Message(
            role = "assistant",
            content = cleanContent,
            toolCall = parsedToolCall
        )
    }
}
