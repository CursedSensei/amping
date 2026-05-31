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
 * Integration tests for [TokenManager].
 *
 * Uses a real temp directory via a mocked [Context] so file I/O is exercised
 * end-to-end. Calls [TokenManager.clearTokens] in [setUp] and [tearDown] to
 * flush the singleton's in-memory cache between tests.
 */
class TokenManagerTest {

    private lateinit var tempDir: File
    private lateinit var context: Context

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("amping_token_test").toFile()
        context = mockk {
            every { filesDir } returns tempDir
        }
        // Reset singleton in-memory cache and disk state before every test
        TokenManager.clearTokens(context)
    }

    @After
    fun tearDown() {
        TokenManager.clearTokens(context)
        tempDir.deleteRecursively()
    }

    // ── Access token ───────────────────────────────────────────────────────────

    @Test
    fun `getAccessToken returns null when nothing has been stored`() {
        assertNull(TokenManager.getAccessToken(context))
    }

    @Test
    fun `saveAccessToken persists token and getAccessToken returns it`() {
        TokenManager.saveAccessToken(context, "my_access_token")
        assertEquals("my_access_token", TokenManager.getAccessToken(context))
    }

    @Test
    fun `saveAccessToken trims leading and trailing whitespace`() {
        TokenManager.saveAccessToken(context, "  trimmed_token  ")
        assertEquals("trimmed_token", TokenManager.getAccessToken(context))
    }

    @Test
    fun `getAccessToken returns cached value without re-reading disk`() {
        TokenManager.saveAccessToken(context, "cached_token")
        // Delete the file; the in-memory cache should still serve the value
        File(tempDir, "access_token.txt").delete()
        assertEquals("cached_token", TokenManager.getAccessToken(context))
    }

    @Test
    fun `getAccessToken reads from disk after cache is cleared`() {
        // Write directly to disk, bypassing the cache
        File(tempDir, "access_token.txt").writeText("disk_token")
        // clearTokens() resets the in-memory cache, forcing a disk read
        TokenManager.clearTokens(context)
        // Re-mock context (clearTokens deleted the file, so write again)
        File(tempDir, "access_token.txt").writeText("disk_token")
        assertEquals("disk_token", TokenManager.getAccessToken(context))
    }

    @Test
    fun `saveAccessToken overwrites a previously stored token`() {
        TokenManager.saveAccessToken(context, "first_token")
        TokenManager.saveAccessToken(context, "second_token")
        assertEquals("second_token", TokenManager.getAccessToken(context))
    }

    // ── Refresh token ──────────────────────────────────────────────────────────

    @Test
    fun `saveRefreshToken persists and getRefreshToken returns it`() {
        TokenManager.saveRefreshToken(context, "my_refresh_token")
        assertEquals("my_refresh_token", TokenManager.getRefreshToken(context))
    }

    @Test
    fun `saveRefreshToken trims whitespace`() {
        TokenManager.saveRefreshToken(context, "  spaced_refresh  ")
        assertEquals("spaced_refresh", TokenManager.getRefreshToken(context))
    }

    @Test
    fun `getRefreshToken seeds default token and writes it to disk when no file exists`() {
        // Ensure file doesn't exist (clearTokens in setUp already handles this)
        assertFalse(File(tempDir, "refresh_token.txt").exists())

        val token = TokenManager.getRefreshToken(context)

        assertNotNull(token)
        // Default should also be persisted to disk
        val diskToken = File(tempDir, "refresh_token.txt").readText().trim()
        assertEquals(token, diskToken)
    }

    @Test
    fun `getRefreshToken returns stored token rather than default when file exists`() {
        TokenManager.saveRefreshToken(context, "custom_refresh_token")
        val token = TokenManager.getRefreshToken(context)
        assertEquals("custom_refresh_token", token)
    }

    // ── clearTokens ────────────────────────────────────────────────────────────

    @Test
    fun `clearTokens removes access token from memory and disk`() {
        TokenManager.saveAccessToken(context, "access")
        TokenManager.clearTokens(context)

        assertNull(TokenManager.getAccessToken(context))
        assertFalse(File(tempDir, "access_token.txt").exists())
    }

    @Test
    fun `clearTokens removes refresh token file from disk`() {
        TokenManager.saveRefreshToken(context, "refresh")
        TokenManager.clearTokens(context)

        assertFalse(File(tempDir, "refresh_token.txt").exists())
    }

    @Test
    fun `clearTokens is idempotent when no tokens are stored`() {
        // Should not throw when called on a clean state
        TokenManager.clearTokens(context)
        TokenManager.clearTokens(context)
        assertNull(TokenManager.getAccessToken(context))
    }
}
