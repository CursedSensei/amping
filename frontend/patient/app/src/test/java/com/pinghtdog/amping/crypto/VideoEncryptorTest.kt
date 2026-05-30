package com.pinghtdog.amping.crypto

import com.pinghtdog.amping.ui.session.VideoEncryptor
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [VideoEncryptor].
 *
 * Tests verify AES/CBC/PKCS5 output structure:
 * - First 16 bytes are the random IV prepended to the ciphertext.
 * - Output length = 16 (IV) + ceil(input / 16) * 16 (PKCS5-padded ciphertext).
 * - Two encryptions of the same plaintext produce different ciphertexts (random IV).
 */
class VideoEncryptorTest {

    // ── Output length ──────────────────────────────────────────────────────────

    @Test
    fun `encrypt empty input produces IV block plus one PKCS5 padding block`() {
        // Empty plaintext → PKCS5 pads to 16 bytes → 16 IV + 16 ciphertext = 32
        val result = VideoEncryptor.encrypt(ByteArray(0))
        assertEquals(32, result.size)
    }

    @Test
    fun `encrypt input smaller than one block has correct output length`() {
        // 10-byte input → padded to 16 bytes → 16 IV + 16 ciphertext = 32
        val result = VideoEncryptor.encrypt(ByteArray(10) { it.toByte() })
        assertEquals(32, result.size)
    }

    @Test
    fun `encrypt input of exactly one block is padded to two blocks`() {
        // AES/PKCS5 always adds padding — 16 bytes → 32 bytes ciphertext → 48 total
        val result = VideoEncryptor.encrypt(ByteArray(16) { it.toByte() })
        assertEquals(48, result.size)
    }

    @Test
    fun `encrypt 100-byte input has correct PKCS5 padded output length`() {
        // 100 bytes → next multiple of 16 is 112 → 16 IV + 112 = 128
        val result = VideoEncryptor.encrypt(ByteArray(100) { it.toByte() })
        assertEquals(128, result.size)
    }

    @Test
    fun `encrypt output length follows formula 16 plus padded ciphertext length`() {
        listOf(1, 15, 16, 17, 64, 255, 1024).forEach { inputSize ->
            val encrypted = VideoEncryptor.encrypt(ByteArray(inputSize) { it.toByte() })
            val expectedCipherLen = ((inputSize / 16) + 1) * 16
            assertEquals(
                "Wrong output size for input of $inputSize bytes",
                16 + expectedCipherLen,
                encrypted.size
            )
        }
    }

    // ── Random IV ──────────────────────────────────────────────────────────────

    @Test
    fun `encrypt same plaintext twice produces different ciphertext`() {
        val input = "Hello VDOT world!".toByteArray()
        val first = VideoEncryptor.encrypt(input)
        val second = VideoEncryptor.encrypt(input)
        assertFalse(
            "Two encryptions of the same plaintext must differ due to random IV",
            first.contentEquals(second)
        )
    }

    @Test
    fun `encrypt IV portion differs across calls`() {
        val input = ByteArray(32) { 0x42 }
        val iv1 = VideoEncryptor.encrypt(input).sliceArray(0..15)
        val iv2 = VideoEncryptor.encrypt(input).sliceArray(0..15)
        assertFalse(
            "IV bytes (first 16) must be random and differ between calls",
            iv1.contentEquals(iv2)
        )
    }

    // ── Output is non-empty and non-trivial ────────────────────────────────────

    @Test
    fun `encrypt result is never all zeros for non-zero input`() {
        val result = VideoEncryptor.encrypt("test payload".toByteArray())
        assertFalse(result.all { it == 0.toByte() })
    }

    @Test
    fun `encrypt large input does not throw`() {
        val bigInput = ByteArray(5 * 1024 * 1024) { it.toByte() } // 5 MB
        val result = VideoEncryptor.encrypt(bigInput)
        assertTrue(result.size > bigInput.size)
    }
}
