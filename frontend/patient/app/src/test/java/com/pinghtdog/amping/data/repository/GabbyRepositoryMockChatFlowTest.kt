package com.pinghtdog.amping.data.repository

import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.ToolCall
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [GabbyRepositoryImpl.getChatResponse] — the offline mock
 * simulation pathway.
 *
 * Tests cover:
 *   - Crisis-keyword detection → emergency_override tool call
 *   - Stage 1: initial greeting → show_symptom_checklist
 *   - Stage 2: symptom submission → transition_to_vdot (with side-effect args)
 *   - Stage 3: VDOT confirmation → trigger_vdot / standby (no tool call)
 *   - Upload complete → transition_to_success
 *   - parseResponse: tool call tags stripped from content
 *
 * [GabbyRepositoryImpl] is instantiated directly (no Hilt) because
 * [getChatResponse] is pure in-memory logic that never touches the HTTP client.
 * [delay] calls are virtualised by [runTest].
 */
class GabbyRepositoryMockChatFlowTest {

    private val repo = GabbyRepositoryImpl()

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun userMsg(content: String) = Message(role = "user", content = content)

    private fun assistantMsg(content: String, toolName: String? = null) = Message(
        role = "assistant",
        content = content,
        toolCall = toolName?.let { ToolCall(name = it) }
    )

    // ── Crisis detection ───────────────────────────────────────────────────────

    @Test
    fun `kill myself triggers emergency_override for adult profile`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("I want to kill myself")), "adult")
        assertEquals("emergency_override", response.toolCall?.name)
        assertTrue(response.content.contains("immense value") || response.content.isNotBlank())
    }

    @Test
    fun `suicide keyword triggers emergency_override for youth profile`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("I'm thinking about suicide")), "youth")
        assertEquals("emergency_override", response.toolCall?.name)
        assertTrue(response.content.contains("super important") || response.content.isNotBlank())
    }

    @Test
    fun `hurt myself triggers emergency_override for senior profile`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("I want to hurt myself")), "senior")
        assertEquals("emergency_override", response.toolCall?.name)
    }

    @Test
    fun `self-harm keyword triggers emergency_override`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("I feel like self-harm")), "adult")
        assertEquals("emergency_override", response.toolCall?.name)
    }

    @Test
    fun `want to die triggers emergency_override`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("I want to die today")), "adult")
        assertEquals("emergency_override", response.toolCall?.name)
    }

    @Test
    fun `emergency_override arguments contain a reason`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("kill myself")), "adult")
        assertFalse(response.toolCall?.arguments?.get("reason").isNullOrBlank())
    }

    // ── Stage 1: first message → show_symptom_checklist ───────────────────────

    @Test
    fun `first user message returns show_symptom_checklist for adult`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("Hello, I'm doing fine")), "adult")
        assertEquals("show_symptom_checklist", response.toolCall?.name)
    }

    @Test
    fun `first user message returns show_symptom_checklist for youth`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("Hey!")), "youth")
        assertEquals("show_symptom_checklist", response.toolCall?.name)
    }

    @Test
    fun `first user message returns show_symptom_checklist for senior`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("Good morning")), "senior")
        assertEquals("show_symptom_checklist", response.toolCall?.name)
    }

    @Test
    fun `senior profile first message includes dear in content`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("Hi")), "senior")
        assertTrue(response.content.lowercase().contains("dear"))
    }

    // ── Stage 2: symptom submission → transition_to_vdot ──────────────────────

    @Test
    fun `after checklist shown symptom message returns transition_to_vdot`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Fill out checklist", "show_symptom_checklist"),
            userMsg("No side effects today")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("transition_to_vdot", response.toolCall?.name)
    }

    @Test
    fun `nausea keyword sets side_effects argument to nausea`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("I have nausea and feel sick")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("nausea", response.toolCall?.arguments?.get("side_effects"))
    }

    @Test
    fun `fatigue keyword sets side_effects argument to fatigue`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("Feeling a lot of fatigue")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("fatigue", response.toolCall?.arguments?.get("side_effects"))
    }

    @Test
    fun `severe nausea sets nausea_severity to Severe`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("I have severe nausea")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("Severe", response.toolCall?.arguments?.get("nausea_severity"))
    }

    @Test
    fun `mild nausea sets nausea_severity to Mild`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("mild nausea today")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("Mild", response.toolCall?.arguments?.get("nausea_severity"))
    }

    @Test
    fun `no symptom keywords sets nausea_severity to None`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("I feel completely fine")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("None", response.toolCall?.arguments?.get("nausea_severity"))
    }

    // ── Stage 3: VDOT confirmation → trigger_vdot or standby ──────────────────

    @Test
    fun `yes confirmation after vdot transition triggers trigger_vdot`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("No symptoms"),
            assistantMsg("Ready to record?", "transition_to_vdot"),
            userMsg("Yes, I am ready to start")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("trigger_vdot", response.toolCall?.name)
    }

    @Test
    fun `ready confirmation triggers trigger_vdot`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("No symptoms"),
            assistantMsg("Ready?", "transition_to_vdot"),
            userMsg("Ready")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("trigger_vdot", response.toolCall?.name)
    }

    @Test
    fun `not yet reply returns no tool call (standby)`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("No symptoms"),
            assistantMsg("Ready?", "transition_to_vdot"),
            userMsg("Not yet, wait a moment")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertNull(response.toolCall)
    }

    @Test
    fun `no reply returns no tool call (standby)`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("No symptoms"),
            assistantMsg("Ready?", "transition_to_vdot"),
            userMsg("No, wait")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertNull(response.toolCall)
    }

    @Test
    fun `trigger_vdot duration_seconds is 15 for youth`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("No symptoms"),
            assistantMsg("Ready?", "transition_to_vdot"),
            userMsg("Yes ready")
        )
        val response = repo.getChatResponse(messages, "youth")
        assertEquals("15", response.toolCall?.arguments?.get("duration_seconds"))
    }

    @Test
    fun `trigger_vdot duration_seconds is 20 for senior`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("No symptoms"),
            assistantMsg("Ready?", "transition_to_vdot"),
            userMsg("Yes, dear")
        )
        val response = repo.getChatResponse(messages, "senior")
        assertEquals("20", response.toolCall?.arguments?.get("duration_seconds"))
    }

    @Test
    fun `trigger_vdot duration_seconds is 15 for adult`() = runTest {
        val messages = listOf(
            userMsg("Hello"),
            assistantMsg("Checklist", "show_symptom_checklist"),
            userMsg("No symptoms"),
            assistantMsg("Ready?", "transition_to_vdot"),
            userMsg("Start")
        )
        val response = repo.getChatResponse(messages, "adult")
        assertEquals("15", response.toolCall?.arguments?.get("duration_seconds"))
    }

    // ── Upload complete → transition_to_success ────────────────────────────────

    @Test
    fun `VDOT upload complete message returns transition_to_success`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("VDOT upload complete.")), "adult")
        assertEquals("transition_to_success", response.toolCall?.name)
    }

    @Test
    fun `vdot upload complete case-insensitive detection works`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("vdot upload complete")), "youth")
        assertEquals("transition_to_success", response.toolCall?.name)
    }

    @Test
    fun `transition_to_success content is not blank`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("VDOT upload complete.")), "adult")
        assertTrue(response.content.isNotBlank())
    }

    // ── parseResponse: tool call tags stripped from displayed content ──────────

    @Test
    fun `response content never contains raw tool_call XML tags`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("Hello")), "adult")
        assertFalse(response.content.contains("<tool_call>"))
        assertFalse(response.content.contains("</tool_call>"))
    }

    @Test
    fun `upload complete response content has no tool_call XML tags`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("VDOT upload complete.")), "adult")
        assertFalse(response.content.contains("<tool_call>"))
    }

    @Test
    fun `emergency response content has no tool_call XML tags`() = runTest {
        val response = repo.getChatResponse(listOf(userMsg("I want to kill myself")), "adult")
        assertFalse(response.content.contains("<tool_call>"))
    }
}
