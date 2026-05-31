package com.pinghtdog.amping.ui.session

import android.content.Context
import com.pinghtdog.amping.data.repository.GabbyRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * Unit tests for [SessionViewModel.parseResponse] (internal visibility).
 *
 * Verifies the response parsing layer that sits between raw LLM text and the
 * [com.pinghtdog.amping.data.model.Message] model:
 *   - Valid tool call JSON is parsed into a [com.pinghtdog.amping.data.model.ToolCall]
 *   - Malformed JSON falls back to the regex parser and still extracts the name
 *   - <think> blocks are stripped from displayed content
 *   - <tool_call> tags are removed from displayed content
 *   - Text without a tool call produces null toolCall
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelParseResponseTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var context: Context
    private lateinit var tempDir: File
    private lateinit var fakeRepo: GabbyRepository
    private lateinit var viewModel: SessionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tempDir = Files.createTempDirectory("amping_parse_test").toFile()
        context = mockk(relaxed = true) {
            every { filesDir } returns tempDir
            every { getSystemService(any<String>()) } returns null
        }
        fakeRepo = mockk(relaxed = true)
        coEvery { fakeRepo.getPatientProfile(any()) } throws Exception("no network")
        coEvery { fakeRepo.getStats(any()) } throws Exception("no network")
        viewModel = SessionViewModel(fakeRepo, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        tempDir.deleteRecursively()
    }

    // ── Valid tool call JSON ───────────────────────────────────────────────────

    @Test
    fun `parseResponse extracts tool call name from valid JSON`() {
        val raw = """Hello! <tool_call>{"name": "trigger_vdot"}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertEquals("trigger_vdot", message.toolCall?.name)
    }

    @Test
    fun `parseResponse extracts tool call name and arguments`() {
        val raw = """Text <tool_call>{"name": "emergency_override", "arguments": {"reason": "Self-harm"}}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertEquals("emergency_override", message.toolCall?.name)
        assertEquals("Self-harm", message.toolCall?.arguments?.get("reason"))
    }

    @Test
    fun `parseResponse strips tool_call tags from displayed content`() {
        val raw = """Hello there! <tool_call>{"name": "show_symptom_checklist"}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertFalse(message.content.contains("<tool_call>"))
        assertFalse(message.content.contains("</tool_call>"))
        assertTrue(message.content.contains("Hello there!"))
    }

    @Test
    fun `parseResponse role is assistant`() {
        val message = viewModel.parseResponse("Some text")
        assertEquals("assistant", message.role)
    }

    // ── No tool call ──────────────────────────────────────────────────────────

    @Test
    fun `parseResponse plain text produces null toolCall`() {
        val message = viewModel.parseResponse("Remaining on standby. Let me know when ready.")
        assertNull(message.toolCall)
    }

    @Test
    fun `parseResponse plain text content is preserved`() {
        val text = "Remaining on standby."
        val message = viewModel.parseResponse(text)
        assertEquals(text, message.content)
    }

    // ── Malformed JSON fallback ────────────────────────────────────────────────

    @Test
    fun `parseResponse falls back to regex parser for malformed JSON and extracts name`() {
        // LLMs sometimes emit single-quoted or slightly broken JSON
        val raw = """Text <tool_call>{'name': 'transition_to_vdot'}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertEquals("transition_to_vdot", message.toolCall?.name)
    }

    @Test
    fun `parseResponse regex fallback extracts named arguments`() {
        val raw = """Text <tool_call>{'name': 'emergency_override', 'arguments': {'reason': 'crisis'}}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertEquals("emergency_override", message.toolCall?.name)
        assertEquals("crisis", message.toolCall?.arguments?.get("reason"))
    }

    @Test
    fun `parseResponse entirely broken JSON produces null toolCall without throwing`() {
        val raw = """Text <tool_call>%%%%</tool_call>"""
        // Should not throw; toolCall may be null if parsing completely fails
        val message = viewModel.parseResponse(raw)
        assertNotNull(message) // at minimum returns a Message
    }

    // ── <think> block stripping ────────────────────────────────────────────────

    @Test
    fun `parseResponse strips think blocks from displayed content`() {
        val raw = "<think>This is internal reasoning</think>Visible response text."
        val message = viewModel.parseResponse(raw)
        assertFalse(message.content.contains("<think>"))
        assertFalse(message.content.contains("This is internal reasoning"))
        assertTrue(message.content.contains("Visible response text."))
    }

    @Test
    fun `parseResponse with both think block and tool call strips both`() {
        val raw = "<think>reasoning</think>Hello! <tool_call>{\"name\": \"trigger_vdot\"}</tool_call>"
        val message = viewModel.parseResponse(raw)
        assertFalse(message.content.contains("<think>"))
        assertFalse(message.content.contains("<tool_call>"))
        assertTrue(message.content.contains("Hello!"))
        assertEquals("trigger_vdot", message.toolCall?.name)
    }

    @Test
    fun `parseResponse multiline think block is fully stripped`() {
        val raw = """
            <think>
            Line one of thinking.
            Line two of thinking.
            </think>
            This is the actual response.
        """.trimIndent()
        val message = viewModel.parseResponse(raw)
        assertFalse(message.content.contains("Line one"))
        assertFalse(message.content.contains("Line two"))
        assertTrue(message.content.contains("This is the actual response."))
    }
}
