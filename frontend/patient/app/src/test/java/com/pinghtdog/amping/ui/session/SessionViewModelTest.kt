package com.pinghtdog.amping.ui.session

import android.content.Context
import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.data.model.SessionPhase
import com.pinghtdog.amping.data.model.ToolCall
import com.pinghtdog.amping.data.repository.GabbyRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * Integration tests for [SessionViewModel].
 *
 * Every [runTest] block ends with [SessionViewModel.clearForTest] to cancel
 * viewModelScope (and its children: fallback-timer coroutine, background-sync
 * loop) *before* runTest's own cleanup sweep. Without this, runTest finds
 * those coroutines still pending and throws [UncompletedCoroutinesError],
 * which cascades into an OOM as the JVM tries to report each subsequent
 * failure.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var context: Context
    private lateinit var tempDir: File
    private lateinit var fakeRepo: GabbyRepository
    private lateinit var viewModel: SessionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tempDir = Files.createTempDirectory("amping_vm_test").toFile()
        context = mockk(relaxed = true) {
            every { filesDir } returns tempDir
            every { getSystemService(any<String>()) } returns null
        }
        fakeRepo = mockk(relaxed = true)
        coEvery { fakeRepo.getPatientProfile(any()) } throws Exception("no network in test")
        coEvery { fakeRepo.getStats(any()) } throws Exception("no network in test")
        viewModel = SessionViewModel(fakeRepo, context)
        viewModel.cancelBackgroundSync()
        viewModel.toggleNetworkMode(false)
    }

    @After
    fun tearDown() {
        viewModel.clearForTest()
        Dispatchers.resetMain()
        tempDir.deleteRecursively()
        unmockkAll()
    }

    // Initial state

    @Test
    fun `initial phase is CONVERSATION`() {
        assertEquals(SessionPhase.CONVERSATION, viewModel.uiState.value.currentPhase)
    }

    @Test
    fun `initial conversationStage is 1`() {
        assertEquals(1, viewModel.uiState.value.conversationStage)
    }

    @Test
    fun `initial emergencyState is null`() {
        assertNull(viewModel.uiState.value.emergencyState)
    }

    @Test
    fun `initial networkError is null`() {
        assertNull(viewModel.uiState.value.networkError)
    }

    @Test
    fun `initial vdotRepromptCount is 0`() {
        assertEquals(0, viewModel.uiState.value.vdotRepromptCount)
    }

    // sendMessage / chatHistory

    @Test
    fun `sendMessage appends user message to chatHistory`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns
                Message(role = "assistant", content = "Got it!")
        viewModel.sendMessage("Hello")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.chatHistory.any { it.role == "user" && it.content == "Hello" })
        viewModel.clearForTest()
    }

    @Test
    fun `sendMessage appends assistant response to chatHistory`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns
                Message(role = "assistant", content = "I hear you!")
        viewModel.sendMessage("Hello")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.chatHistory.any { it.role == "assistant" && it.content == "I hear you!" })
        viewModel.clearForTest()
    }

    @Test
    fun `sendMessage clears networkError before processing`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns
                Message(role = "assistant", content = "OK")
        viewModel.sendMessage("Anything")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.networkError)
        viewModel.clearForTest()
    }

    @Test
    fun `sendMessage multiple times accumulates messages in history`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns
                Message(role = "assistant", content = "Reply")
        viewModel.sendMessage("First")
        advanceUntilIdle()
        viewModel.sendMessage("Second")
        advanceUntilIdle()
        val userMessages = viewModel.uiState.value.chatHistory.filter { it.role == "user" }
        assertTrue(userMessages.any { it.content == "First" })
        assertTrue(userMessages.any { it.content == "Second" })
        viewModel.clearForTest()
    }

    // Phase transitions via tool calls

    @Test
    fun `show_symptom_checklist tool call sets pendingToolCallName`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns Message(
            role = "assistant", content = "Please fill checklist",
            toolCall = ToolCall(name = "show_symptom_checklist")
        )
        viewModel.sendMessage("Hello")
        assertEquals("show_symptom_checklist", viewModel.uiState.value.pendingToolCallName)
        viewModel.clearForTest()
    }

    @Test
    fun `executePendingToolCall show_symptom_checklist transitions to SYMPTOM_LOGGING`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns Message(
            role = "assistant", content = "Checklist!",
            toolCall = ToolCall(name = "show_symptom_checklist")
        )
        viewModel.sendMessage("Hello")
        advanceUntilIdle()
        viewModel.executePendingToolCall()
        assertEquals(SessionPhase.SYMPTOM_LOGGING, viewModel.uiState.value.currentPhase)
        assertNull(viewModel.uiState.value.pendingToolCallName)
        viewModel.clearForTest()
    }

    @Test
    fun `executePendingToolCall transition_to_vdot sets conversationStage to 3`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns Message(
            role = "assistant", content = "VDOT time",
            toolCall = ToolCall(name = "transition_to_vdot")
        )
        viewModel.sendMessage("No symptoms")
        advanceUntilIdle()
        viewModel.executePendingToolCall()
        assertEquals(3, viewModel.uiState.value.conversationStage)
        assertNull(viewModel.uiState.value.pendingToolCallName)
        viewModel.clearForTest()
    }

    @Test
    fun `executePendingToolCall trigger_vdot transitions to VDOT_CAPTURE`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns Message(
            role = "assistant", content = "Starting camera",
            toolCall = ToolCall(name = "trigger_vdot")
        )
        viewModel.sendMessage("Ready")
        advanceUntilIdle()
        viewModel.executePendingToolCall()
        assertEquals(SessionPhase.VDOT_CAPTURE, viewModel.uiState.value.currentPhase)
        viewModel.clearForTest()
    }

    @Test
    fun `executePendingToolCall transition_to_success transitions to SUCCESS`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns Message(
            role = "assistant", content = "All done!",
            toolCall = ToolCall(name = "transition_to_success")
        )
        viewModel.sendMessage("Upload complete")
        advanceUntilIdle()
        viewModel.executePendingToolCall()
        assertEquals(SessionPhase.SUCCESS, viewModel.uiState.value.currentPhase)
        viewModel.clearForTest()
    }

    @Test
    fun `executePendingToolCall with null pending call is a no-op`() = runTest {
        val phaseBefore = viewModel.uiState.value.currentPhase
        viewModel.executePendingToolCall()
        assertEquals(phaseBefore, viewModel.uiState.value.currentPhase)
        viewModel.clearForTest()
    }

    // vdotRepromptCount

    @Test
    fun `vdotRepromptCount increments when stage 3 response has no trigger_vdot tool call`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returnsMany listOf(
            Message(
                role = "assistant", content = "VDOT transition",
                toolCall = ToolCall(name = "transition_to_vdot")
            ),
            Message(role = "assistant", content = "Not yet", toolCall = null)
        )
        viewModel.sendMessage("No symptoms")
        advanceUntilIdle()
        viewModel.executePendingToolCall() // now at stage 3

        val countBefore = viewModel.uiState.value.vdotRepromptCount
        viewModel.sendMessage("Not yet")
        advanceUntilIdle()

        assertEquals(countBefore + 1, viewModel.uiState.value.vdotRepromptCount)
        viewModel.clearForTest()
    }

    // Emergency override

    @Test
    fun `emergency_override tool call sets emergencyState immediately`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } returns Message(
            role = "assistant", content = "Emergency!",
            toolCall = ToolCall(name = "emergency_override", arguments = mapOf("reason" to "Self-harm detected"))
        )
        viewModel.sendMessage("I want to hurt myself")
        advanceUntilIdle()
        assertEquals("Self-harm detected", viewModel.uiState.value.emergencyState)
        viewModel.clearForTest()
    }

    @Test
    fun `triggerEmergencyOverride sets emergencyState to given reason`() {
        viewModel.triggerEmergencyOverride("Test override reason")
        assertEquals("Test override reason", viewModel.uiState.value.emergencyState)
    }

    @Test
    fun `triggerEmergencyOverride with empty string sets emergencyState to empty`() {
        viewModel.triggerEmergencyOverride("")
        assertEquals("", viewModel.uiState.value.emergencyState)
    }

    // Symptom selection

    @Test
    fun `selectSymptom checked adds symptom to selectedSymptoms`() {
        viewModel.selectSymptom("Nausea", true)
        assertTrue(viewModel.uiState.value.selectedSymptoms.contains("Nausea"))
    }

    @Test
    fun `selectSymptom unchecked removes symptom from selectedSymptoms`() {
        viewModel.selectSymptom("Nausea", true)
        viewModel.selectSymptom("Nausea", false)
        assertFalse(viewModel.uiState.value.selectedSymptoms.contains("Nausea"))
    }

    @Test
    fun `selectSymptom multiple symptoms are all retained`() {
        viewModel.selectSymptom("Nausea", true)
        viewModel.selectSymptom("Fatigue", true)
        val selected = viewModel.uiState.value.selectedSymptoms
        assertTrue(selected.contains("Nausea"))
        assertTrue(selected.contains("Fatigue"))
    }

    @Test
    fun `selectSymptom checking then unchecking leaves selectedSymptoms empty`() {
        viewModel.selectSymptom("Nausea", true)
        viewModel.selectSymptom("Nausea", false)
        assertTrue(viewModel.uiState.value.selectedSymptoms.isEmpty())
    }

    @Test
    fun `removing Nausea resets nauseaSeverity to None`() {
        viewModel.selectSymptom("Nausea", true)
        viewModel.selectNauseaSeverity("Severe")
        viewModel.selectSymptom("Nausea", false)
        assertEquals("None", viewModel.uiState.value.nauseaSeverity)
    }

    @Test
    fun `removing a different symptom does not reset nauseaSeverity`() {
        viewModel.selectSymptom("Nausea", true)
        viewModel.selectSymptom("Fatigue", true)
        viewModel.selectNauseaSeverity("Mild")
        viewModel.selectSymptom("Fatigue", false)
        assertEquals("Mild", viewModel.uiState.value.nauseaSeverity)
    }

    @Test
    fun `selectNauseaSeverity updates nauseaSeverity`() {
        viewModel.selectNauseaSeverity("Mild")
        assertEquals("Mild", viewModel.uiState.value.nauseaSeverity)
    }

    // Network error dismissal

    @Test
    fun `dismissNetworkError clears networkError`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } throws Exception("timeout")
        viewModel.sendMessage("Hello")
        advanceUntilIdle()
        viewModel.dismissNetworkError()
        assertNull(viewModel.uiState.value.networkError)
        viewModel.clearForTest()
    }

    @Test
    fun `repo exception sets networkError with message`() = runTest {
        coEvery { fakeRepo.getChatResponse(any(), any()) } throws Exception("connection refused")
        viewModel.sendMessage("Hello")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.networkError)
        viewModel.clearForTest()
    }

    // Profile selection

    @Test
    fun `selectProfile updates activeProfile`() {
        viewModel.selectProfile("senior")
        assertEquals("senior", viewModel.uiState.value.activeProfile)
    }

    @Test
    fun `selectProfile resets phase to CONVERSATION`() = runTest {
        viewModel.forcePhase(SessionPhase.SUCCESS)
        viewModel.selectProfile("senior")
        advanceUntilIdle()
        assertEquals(SessionPhase.CONVERSATION, viewModel.uiState.value.currentPhase)
        viewModel.clearForTest()
    }

    @Test
    fun `selectProfile with same profile does not re-initialise state`() {
        val profileBefore = viewModel.uiState.value.activeProfile
        viewModel.selectProfile(profileBefore)
        assertEquals(profileBefore, viewModel.uiState.value.activeProfile)
    }

    // Debug panel helpers

    @Test
    fun `forcePhase SYMPTOM_LOGGING sets phase and conversationStage to 2`() {
        viewModel.forcePhase(SessionPhase.SYMPTOM_LOGGING)
        assertEquals(SessionPhase.SYMPTOM_LOGGING, viewModel.uiState.value.currentPhase)
        assertEquals(2, viewModel.uiState.value.conversationStage)
    }

    @Test
    fun `forcePhase VDOT_CAPTURE sets currentPhase`() {
        viewModel.forcePhase(SessionPhase.VDOT_CAPTURE)
        assertEquals(SessionPhase.VDOT_CAPTURE, viewModel.uiState.value.currentPhase)
    }

    @Test
    fun `forcePhase CONVERSATION resets conversationStage to 1`() {
        viewModel.forcePhase(SessionPhase.CONVERSATION)
        assertEquals(1, viewModel.uiState.value.conversationStage)
    }

    @Test
    fun `forcePhase resets vdotRepromptCount to 0`() {
        viewModel.forcePhase(SessionPhase.VDOT_CAPTURE)
        assertEquals(0, viewModel.uiState.value.vdotRepromptCount)
    }

    @Test
    fun `bypassToolCallToVideoRecording sets phase to VDOT_CAPTURE`() {
        viewModel.bypassToolCallToVideoRecording()
        assertEquals(SessionPhase.VDOT_CAPTURE, viewModel.uiState.value.currentPhase)
    }

    // completeRecording

    @Test
    fun `completeRecording stores the video file path`() {
        viewModel.completeRecording("/storage/emulated/vdot.mp4")
        assertEquals("/storage/emulated/vdot.mp4", viewModel.uiState.value.recordedVideoPath)
    }

    @Test
    fun `completeRecording transitions to VDOT_REVIEW`() {
        viewModel.completeRecording("/storage/emulated/vdot.mp4")
        assertEquals(SessionPhase.VDOT_REVIEW, viewModel.uiState.value.currentPhase)
    }

    @Test
    fun `completeRecording called twice overwrites the previous path`() {
        viewModel.completeRecording("/storage/first.mp4")
        viewModel.completeRecording("/storage/second.mp4")
        assertEquals("/storage/second.mp4", viewModel.uiState.value.recordedVideoPath)
    }
}
