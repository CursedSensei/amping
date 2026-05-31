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
 *   - think blocks are stripped from displayed content
 *   - tool_call tags are removed from displayed content
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

    // Valid tool call JSON

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
    fun `parseResponse sets role to assistant`() {
        assertEquals("assistant", viewModel.parseResponse("Some text").role)
    }

    @Test
    fun `parseResponse tool call with empty arguments map produces empty map not null`() {
        val raw = """Text <tool_call>{"name": "transition_to_success", "arguments": {}}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertEquals("transition_to_success", message.toolCall?.name)
        assertNotNull(message.toolCall?.arguments)
        assertTrue(message.toolCall!!.arguments.isEmpty())
    }

    @Test
    fun `parseResponse tool call with no arguments key defaults to empty map`() {
        val raw = """Text <tool_call>{"name": "show_symptom_checklist"}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertNotNull(message.toolCall?.arguments)
    }

    // No tool call

    @Test
    fun `parseResponse plain text produces null toolCall`() {
        assertNull(viewModel.parseResponse("Remaining on standby.").toolCall)
    }

    @Test
    fun `parseResponse plain text content is preserved exactly`() {
        val text = "Remaining on standby."
        assertEquals(text, viewModel.parseResponse(text).content)
    }

    @Test
    fun `parseResponse empty string produces null toolCall and empty content`() {
        val message = viewModel.parseResponse("")
        assertNull(message.toolCall)
        assertEquals("", message.content)
    }

    @Test
    fun `parseResponse whitespace-only string returns null toolCall`() {
        assertNull(viewModel.parseResponse("   \n   ").toolCall)
    }

    @Test
    fun `parseResponse with only tool_call tags leaves content blank after stripping`() {
        val raw = """<tool_call>{"name": "trigger_vdot"}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertTrue(message.content.isBlank())
        assertEquals("trigger_vdot", message.toolCall?.name)
    }

    // Malformed JSON fallback

    @Test
    fun `parseResponse falls back to regex parser for single-quoted JSON`() {
        val raw = """Text <tool_call>{'name': 'transition_to_vdot'}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertEquals("transition_to_vdot", message.toolCall?.name)
    }

    @Test
    fun `parseResponse regex fallback extracts named arguments from single-quoted JSON`() {
        val raw = """Text <tool_call>{'name': 'emergency_override', 'arguments': {'reason': 'crisis'}}</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertEquals("emergency_override", message.toolCall?.name)
        assertEquals("crisis", message.toolCall?.arguments?.get("reason"))
    }

    @Test
    fun `parseResponse completely unparseable tool call does not throw`() {
        val raw = """Text <tool_call>%%%%</tool_call>"""
        val message = viewModel.parseResponse(raw)
        assertNotNull(message)
    }

    // think block stripping

    @Test
    fun `parseResponse strips think block from displayed content`() {
        val raw = "<think>Internal reasoning</think>Visible response."
        val message = viewModel.parseResponse(raw)
        assertFalse(message.content.contains("<think>"))
        assertFalse(message.content.contains("Internal reasoning"))
        assertTrue(message.content.contains("Visible response."))
    }

    @Test
    fun `parseResponse with think block and tool call strips both`() {
        val raw = "<think>reasoning</think>Hello! <tool_call>{\"name\": \"trigger_vdot\"}</tool_call>"
        val message = viewModel.parseResponse(raw)
        assertFalse(message.content.contains("<think>"))
        assertFalse(message.content.contains("<tool_call>"))
        assertTrue(message.content.contains("Hello!"))
        assertEquals("trigger_vdot", message.toolCall?.name)
    }

    @Test
    fun `parseResponse strips multiline think block completely`() {
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

    @Test
    fun `parseResponse with only a think block leaves blank content`() {
        val raw = "<think>All internal, nothing visible.</think>"
        val message = viewModel.parseResponse(raw)
        assertTrue(message.content.isBlank())
        assertNull(message.toolCall)
    }
}
