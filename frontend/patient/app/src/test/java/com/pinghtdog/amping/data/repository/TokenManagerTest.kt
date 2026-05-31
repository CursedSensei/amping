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
 * end-to-end. Calls [TokenManager.clearTokens] in setUp and tearDown to
 * flush the singleton in-memory cache between tests.
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
        TokenManager.clearTokens(context)
    }

    @After
    fun tearDown() {
        TokenManager.clearTokens(context)
        tempDir.deleteRecursively()
    }

    // Access token

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
    fun `saveAccessToken with only whitespace is treated as empty and returns null`() {
        // After trimming, "   " becomes "" which is stored as empty; getAccessToken
        // reads the file and calls ifEmpty { null }, returning null.
        TokenManager.clearTokens(context) // clear cache
        File(tempDir, "access_token.txt").writeText("   ")
        assertNull(TokenManager.getAccessToken(context))
    }

    @Test
    fun `getAccessToken returns cached value without re-reading disk`() {
        TokenManager.saveAccessToken(context, "cached_token")
        File(tempDir, "access_token.txt").delete()
        assertEquals("cached_token", TokenManager.getAccessToken(context))
    }

    @Test
    fun `getAccessToken reads from disk when cache has been cleared`() {
        File(tempDir, "access_token.txt").writeText("disk_token")
        TokenManager.clearTokens(context)
        File(tempDir, "access_token.txt").writeText("disk_token")
        assertEquals("disk_token", TokenManager.getAccessToken(context))
    }

    @Test
    fun `saveAccessToken overwrites a previously stored token`() {
        TokenManager.saveAccessToken(context, "first_token")
        TokenManager.saveAccessToken(context, "second_token")
        assertEquals("second_token", TokenManager.getAccessToken(context))
    }

    @Test
    fun `saveAccessToken writes the token to disk`() {
        TokenManager.saveAccessToken(context, "disk_check")
        val onDisk = File(tempDir, "access_token.txt").readText().trim()
        assertEquals("disk_check", onDisk)
    }

    // Refresh token

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
    fun `getRefreshToken seeds default token to disk when no file exists`() {
        assertFalse(File(tempDir, "refresh_token.txt").exists())
        val token = TokenManager.getRefreshToken(context)
        assertNotNull(token)
        val onDisk = File(tempDir, "refresh_token.txt").readText().trim()
        assertEquals(token, onDisk)
    }

    @Test
    fun `getRefreshToken returns stored token rather than default when file exists`() {
        TokenManager.saveRefreshToken(context, "custom_refresh_token")
        assertEquals("custom_refresh_token", TokenManager.getRefreshToken(context))
    }

    @Test
    fun `getRefreshToken called twice returns the same value`() {
        TokenManager.saveRefreshToken(context, "stable_token")
        assertEquals(TokenManager.getRefreshToken(context), TokenManager.getRefreshToken(context))
    }

    @Test
    fun `getRefreshToken with empty file falls back to seeding default`() {
        // An empty refresh_token.txt is treated as missing — the default is seeded.
        File(tempDir, "refresh_token.txt").writeText("")
        val token = TokenManager.getRefreshToken(context)
        assertNotNull(token)
        assertTrue(token!!.isNotBlank())
    }

    // clearTokens

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
    fun `clearTokens is idempotent when called on a clean state`() {
        TokenManager.clearTokens(context)
        TokenManager.clearTokens(context)
        assertNull(TokenManager.getAccessToken(context))
    }

    @Test
    fun `clearTokens allows a fresh token to be stored afterward`() {
        TokenManager.saveAccessToken(context, "old_token")
        TokenManager.clearTokens(context)
        TokenManager.saveAccessToken(context, "new_token")
        assertEquals("new_token", TokenManager.getAccessToken(context))
    }
}
