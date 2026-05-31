package com.pinghtdog.amping.data.repository

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * Integration tests for [OfflineQueueManager].
 *
 * Uses a real temp directory via a mocked [Context] so that file I/O is
 * exercised end-to-end without requiring an Android device.
 */
class OfflineQueueManagerTest {

    private lateinit var tempDir: File
    private lateinit var context: Context

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("amping_queue_test").toFile()
        context = mockk {
            every { filesDir } returns tempDir
        }
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    // ── getQueue ───────────────────────────────────────────────────────────────

    @Test
    fun `getQueue returns empty list when queue file does not exist`() {
        val queue = OfflineQueueManager.getQueue(context)
        assertTrue(queue.isEmpty())
    }

    @Test
    fun `getQueue returns empty list when queue file contains corrupted JSON`() {
        File(tempDir, "offline_queue.json").writeText("{ not valid json [[[")
        val queue = OfflineQueueManager.getQueue(context)
        assertTrue(queue.isEmpty())
    }

    @Test
    fun `getQueue returns empty list when queue file is empty`() {
        File(tempDir, "offline_queue.json").writeText("")
        val queue = OfflineQueueManager.getQueue(context)
        assertTrue(queue.isEmpty())
    }

    // ── addEntry ───────────────────────────────────────────────────────────────

    @Test
    fun `addEntry persists entry and getQueue returns it`() {
        OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")

        val queue = OfflineQueueManager.getQueue(context)
        assertEquals(1, queue.size)
        assertEquals("/tmp/video.enc", queue[0].localEncryptedPath)
        assertEquals("youth", queue[0].profile)
    }

    @Test
    fun `addEntry sets initial status to Pending`() {
        OfflineQueueManager.addEntry(context, "/tmp/video.enc", "adult")
        assertEquals("Pending", OfflineQueueManager.getQueue(context)[0].status)
    }

    @Test
    fun `addEntry sets initial retryCount to zero`() {
        OfflineQueueManager.addEntry(context, "/tmp/video.enc", "adult")
        assertEquals(0, OfflineQueueManager.getQueue(context)[0].retryCount)
    }

    @Test
    fun `addEntry generates a non-blank UUID id`() {
        val entry = OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")
        assertTrue(entry.id.isNotBlank())
    }

    @Test
    fun `addEntry with adherenceDayID stores it on the entry`() {
        OfflineQueueManager.addEntry(context, "/tmp/video.enc", "adult", adherenceDayID = 99L)
        assertEquals(99L, OfflineQueueManager.getQueue(context)[0].adherenceDayID)
    }

    @Test
    fun `addEntry without adherenceDayID leaves it null`() {
        OfflineQueueManager.addEntry(context, "/tmp/video.enc", "adult")
        assertNull(OfflineQueueManager.getQueue(context)[0].adherenceDayID)
    }

    @Test
    fun `addEntry multiple entries generates unique IDs`() {
        OfflineQueueManager.addEntry(context, "/tmp/a.enc", "youth")
        OfflineQueueManager.addEntry(context, "/tmp/b.enc", "senior")

        val queue = OfflineQueueManager.getQueue(context)
        assertNotEquals(queue[0].id, queue[1].id)
    }

    @Test
    fun `addEntry multiple entries preserves insertion order`() {
        OfflineQueueManager.addEntry(context, "/tmp/first.enc", "youth")
        OfflineQueueManager.addEntry(context, "/tmp/second.enc", "adult")
        OfflineQueueManager.addEntry(context, "/tmp/third.enc", "senior")

        val queue = OfflineQueueManager.getQueue(context)
        assertEquals(3, queue.size)
        assertEquals("/tmp/first.enc", queue[0].localEncryptedPath)
        assertEquals("/tmp/second.enc", queue[1].localEncryptedPath)
        assertEquals("/tmp/third.enc", queue[2].localEncryptedPath)
    }

    @Test
    fun `addEntry returns the entry that was added`() {
        val entry = OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth", 42L)
        assertEquals("/tmp/video.enc", entry.localEncryptedPath)
        assertEquals("youth", entry.profile)
        assertEquals(42L, entry.adherenceDayID)
    }

    // ── updateEntryStatus ──────────────────────────────────────────────────────

    @Test
    fun `updateEntryStatus changes status of matching entry`() {
        val entry = OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")
        OfflineQueueManager.updateEntryStatus(context, entry.id, "Uploading")

        assertEquals("Uploading", OfflineQueueManager.getQueue(context)[0].status)
    }

    @Test
    fun `updateEntryStatus with retryCount updates the retry counter`() {
        val entry = OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")
        OfflineQueueManager.updateEntryStatus(context, entry.id, "Failed", retryCount = 3)

        assertEquals(3, OfflineQueueManager.getQueue(context)[0].retryCount)
    }

    @Test
    fun `updateEntryStatus without retryCount preserves existing retryCount`() {
        val entry = OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")
        OfflineQueueManager.updateEntryStatus(context, entry.id, "Failed", retryCount = 2)
        OfflineQueueManager.updateEntryStatus(context, entry.id, "Uploading") // no retryCount arg

        assertEquals(2, OfflineQueueManager.getQueue(context)[0].retryCount)
    }

    @Test
    fun `updateEntryStatus with unknown id does not modify the queue`() {
        OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")
        OfflineQueueManager.updateEntryStatus(context, "nonexistent-id", "Failed")

        assertEquals("Pending", OfflineQueueManager.getQueue(context)[0].status)
    }

    @Test
    fun `updateEntryStatus only affects the targeted entry`() {
        val a = OfflineQueueManager.addEntry(context, "/tmp/a.enc", "youth")
        OfflineQueueManager.addEntry(context, "/tmp/b.enc", "adult")

        OfflineQueueManager.updateEntryStatus(context, a.id, "Uploading")

        val queue = OfflineQueueManager.getQueue(context)
        assertEquals("Uploading", queue.first { it.id == a.id }.status)
        assertEquals("Pending", queue.first { it.localEncryptedPath == "/tmp/b.enc" }.status)
    }

    // ── removeEntry ────────────────────────────────────────────────────────────

    @Test
    fun `removeEntry removes the matching entry from the queue`() {
        val entry = OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")
        OfflineQueueManager.removeEntry(context, entry.id)

        assertTrue(OfflineQueueManager.getQueue(context).isEmpty())
    }

    @Test
    fun `removeEntry deletes the encrypted file from disk`() {
        val encFile = File(tempDir, "video.enc").also { it.writeBytes(ByteArray(32) { 0x42 }) }
        assertTrue(encFile.exists())

        val entry = OfflineQueueManager.addEntry(context, encFile.absolutePath, "youth")
        OfflineQueueManager.removeEntry(context, entry.id)

        assertFalse(encFile.exists())
    }

    @Test
    fun `removeEntry with nonexistent id leaves queue unchanged`() {
        OfflineQueueManager.addEntry(context, "/tmp/video.enc", "youth")
        OfflineQueueManager.removeEntry(context, "no-such-id")

        assertEquals(1, OfflineQueueManager.getQueue(context).size)
    }

    @Test
    fun `removeEntry only removes the targeted entry`() {
        val a = OfflineQueueManager.addEntry(context, "/tmp/a.enc", "youth")
        OfflineQueueManager.addEntry(context, "/tmp/b.enc", "adult")

        OfflineQueueManager.removeEntry(context, a.id)

        val queue = OfflineQueueManager.getQueue(context)
        assertEquals(1, queue.size)
        assertEquals("/tmp/b.enc", queue[0].localEncryptedPath)
    }

    @Test
    fun `removeEntry does not throw when encrypted file path does not exist on disk`() {
        val entry = OfflineQueueManager.addEntry(context, "/nonexistent/path/video.enc", "youth")
        // Should not throw even though the file path doesn't exist
        OfflineQueueManager.removeEntry(context, entry.id)
        assertTrue(OfflineQueueManager.getQueue(context).isEmpty())
    }
}
