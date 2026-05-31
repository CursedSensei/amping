package com.pinghtdog.amping.ui

import android.content.Context
import com.pinghtdog.amping.api_schemas.MobilePenaltyEvent
import com.pinghtdog.amping.api_schemas.MobilePatientProfileResponse
import com.pinghtdog.amping.api_schemas.MobileStatsResponse
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
 * Integration tests for [DashboardViewModel].
 *
 * Uses a MockK [GabbyRepository] and mocked [Context] backed by a real temp
 * directory. Tests verify state population on successful network load, error
 * handling, offline fallback, and profile / toggle mutations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var context: Context
    private lateinit var tempDir: File
    private lateinit var fakeRepo: GabbyRepository

    // Convenience builders

    private fun patientProfile(
        firstname: String = "Arthur",
        currentDay: Long = 5L,
        totalDays: Long = 180L
    ) = MobilePatientProfileResponse(
        birthyear = 1985L,
        contact = "555-0100",
        currentDay = currentDay,
        email = "test@amping.app",
        firstname = firstname,
        id = 1L,
        lastname = "Smith",
        regimenStart = "2024-01-01",
        totalDays = totalDays
    )

    private fun stats(
        currentStreak: Long = 7L,
        bestStreak: Long = 14L,
        heartQuota: Long = 3L,
        totalRegimenDays: Long = 180L,
        gracePeriodHours: Long = 48L,
        penaltyHistory: List<MobilePenaltyEvent> = emptyList()
    ) = MobileStatsResponse(
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        heartQuota = heartQuota,
        totalRegimenDays = totalRegimenDays,
        gracePeriodHours = gracePeriodHours,
        penaltyHistory = penaltyHistory
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tempDir = Files.createTempDirectory("amping_dash_test").toFile()
        context = mockk(relaxed = true) {
            every { filesDir } returns tempDir
        }
        fakeRepo = mockk(relaxed = true)
        coEvery { fakeRepo.getPatientProfile(any()) } throws Exception("no network")
        coEvery { fakeRepo.getStats(any()) } throws Exception("no network")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        tempDir.deleteRecursively()
        unmockkAll()
    }

    private fun createViewModel() = DashboardViewModel(fakeRepo, context)

    // Network load — happy path

    @Test
    fun `loadDashboardData populates firstname from profile`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile(firstname = "Arthur")
        coEvery { fakeRepo.getStats(any()) } returns stats()
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertEquals("Arthur", vm.uiState.value.firstname)
    }

    @Test
    fun `loadDashboardData populates currentStreak from stats`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats(currentStreak = 7L)
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertEquals(7, vm.uiState.value.currentStreak)
    }

    @Test
    fun `loadDashboardData populates bestStreak from stats`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats(bestStreak = 21L)
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertEquals(21, vm.uiState.value.bestStreak)
    }

    @Test
    fun `loadDashboardData populates heartQuota from stats`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats(heartQuota = 5L)
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertEquals(5, vm.uiState.value.heartQuota)
    }

    @Test
    fun `loadDashboardData populates currentDay from profile`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile(currentDay = 12L)
        coEvery { fakeRepo.getStats(any()) } returns stats()
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertEquals(12, vm.uiState.value.currentDay)
    }

    @Test
    fun `loadDashboardData populates gracePeriodHours from stats`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats(gracePeriodHours = 72L)
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertEquals(72L, vm.uiState.value.gracePeriodHours)
    }

    @Test
    fun `loadDashboardData populates totalRegimenDays from stats`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats(totalRegimenDays = 90L)
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertEquals(90, vm.uiState.value.totalRegimenDays)
    }

    @Test
    fun `loadDashboardData with non-empty penaltyHistory does not crash`() = runTest {
        val penalty = MobilePenaltyEvent(date = "2024-05-01", label = "Missed dose", tier = 1L)
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats(penaltyHistory = listOf(penalty))
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadDashboardData sets isLoading to false after successful fetch`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats()
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadDashboardData clears errorMessage on success`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats()
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `loadDashboardData can be called multiple times without error`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } returns stats()
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        vm.loadDashboardData()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    // Error handling

    @Test
    fun `loadDashboardData sets errorMessage when getPatientProfile throws`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } throws Exception("server down")
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `loadDashboardData sets isLoading to false on error`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } throws Exception("timeout")
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadDashboardData sets errorMessage when getStats throws`() = runTest {
        coEvery { fakeRepo.getPatientProfile(any()) } returns patientProfile()
        coEvery { fakeRepo.getStats(any()) } throws Exception("stats unavailable")
        val vm = createViewModel()
        vm.toggleNetworkMode(true)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)
    }

    // Offline mode

    @Test
    fun `loadDashboardData in offline mode does not call repository or set error`() = runTest {
        val vm = createViewModel()
        vm.toggleNetworkMode(false)
        advanceUntilIdle()
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `toggleNetworkMode false sets isNetworkMode to false`() = runTest {
        val vm = createViewModel()
        vm.toggleNetworkMode(false)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isNetworkMode)
    }

    // Profile selection

    @Test
    fun `selectProfile KIDS sets profileType and firstname to Leo`() {
        val vm = createViewModel()
        vm.selectProfile(ProfileType.KIDS)
        assertEquals(ProfileType.KIDS, vm.uiState.value.profileType)
        assertEquals("Leo", vm.uiState.value.firstname)
    }

    @Test
    fun `selectProfile ADULTS sets profileType and firstname to Arthur`() {
        val vm = createViewModel()
        vm.selectProfile(ProfileType.ADULTS)
        assertEquals(ProfileType.ADULTS, vm.uiState.value.profileType)
        assertEquals("Arthur", vm.uiState.value.firstname)
    }

    @Test
    fun `selectProfile SENIORS sets profileType and firstname to Lola`() {
        val vm = createViewModel()
        vm.selectProfile(ProfileType.SENIORS)
        assertEquals(ProfileType.SENIORS, vm.uiState.value.profileType)
        assertEquals("Lola", vm.uiState.value.firstname)
    }

    @Test
    fun `selectProfile can be switched between all three types`() {
        val vm = createViewModel()
        vm.selectProfile(ProfileType.KIDS)
        vm.selectProfile(ProfileType.ADULTS)
        vm.selectProfile(ProfileType.SENIORS)
        assertEquals(ProfileType.SENIORS, vm.uiState.value.profileType)
    }

    // toggleTodayTaken

    @Test
    fun `toggleTodayTaken flips isTodayTaken from false to true`() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.isTodayTaken)
        vm.toggleTodayTaken()
        assertTrue(vm.uiState.value.isTodayTaken)
    }

    @Test
    fun `toggleTodayTaken flips isTodayTaken back to false on second call`() {
        val vm = createViewModel()
        vm.toggleTodayTaken()
        vm.toggleTodayTaken()
        assertFalse(vm.uiState.value.isTodayTaken)
    }

    @Test
    fun `toggleTodayTaken called three times ends on true`() {
        val vm = createViewModel()
        repeat(3) { vm.toggleTodayTaken() }
        assertTrue(vm.uiState.value.isTodayTaken)
    }
}
