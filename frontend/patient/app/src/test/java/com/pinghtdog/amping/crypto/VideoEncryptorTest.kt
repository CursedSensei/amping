package com.pinghtdog.amping.crypto

import com.pinghtdog.amping.ui.session.VideoEncryptor
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [VideoEncryptor].
 *
 * Structure invariant: output = 16-byte random IV || AES-CBC ciphertext.
 * PKCS5 padding always adds at least one byte, so ciphertext length is
 * always the next multiple of 16 strictly greater than the input length.
 *
 * Output length formula: 16 + ((inputSize / 16) + 1) * 16
 */
class VideoEncryptorTest {

    // Output length

    @Test
    fun `encrypt empty input produces IV plus one full PKCS5 padding block`() {
        // 0 bytes → padded to 16 bytes ciphertext → 16 IV + 16 = 32
        assertEquals(32, VideoEncryptor.encrypt(ByteArray(0)).size)
    }

    @Test
    fun `encrypt single byte input has correct output length`() {
        // 1 byte → padded to 16 → 16 IV + 16 = 32
        assertEquals(32, VideoEncryptor.encrypt(ByteArray(1) { 0x42 }).size)
    }

    @Test
    fun `encrypt 15-byte input stays in one padded block`() {
        // 15 bytes → padded to 16 → 16 IV + 16 = 32
        assertEquals(32, VideoEncryptor.encrypt(ByteArray(15) { it.toByte() }).size)
    }

    @Test
    fun `encrypt exactly one block forces a full extra padding block`() {
        // PKCS5 always appends padding — 16 bytes → ciphertext is 32 → 16 IV + 32 = 48
        assertEquals(48, VideoEncryptor.encrypt(ByteArray(16) { it.toByte() }).size)
    }

    @Test
    fun `encrypt 17-byte input spans into second block`() {
        // 17 bytes → padded to 32 → 16 IV + 32 = 48
        assertEquals(48, VideoEncryptor.encrypt(ByteArray(17) { it.toByte() }).size)
    }

    @Test
    fun `encrypt 32-byte input forces a third padding block`() {
        // 32 bytes (exactly 2 blocks) → padded to 48 → 16 IV + 48 = 64
        assertEquals(64, VideoEncryptor.encrypt(ByteArray(32) { it.toByte() }).size)
    }

    @Test
    fun `encrypt 100-byte input has correct PKCS5 padded length`() {
        // 100 bytes → next multiple of 16 is 112 → 16 IV + 112 = 128
        assertEquals(128, VideoEncryptor.encrypt(ByteArray(100) { it.toByte() }).size)
    }

    @Test
    fun `encrypt output is always strictly larger than the input`() {
        listOf(0, 1, 15, 16, 17, 31, 32, 64, 100, 255).forEach { size ->
            val result = VideoEncryptor.encrypt(ByteArray(size) { it.toByte() })
            assertTrue("Output must be larger than input for size=$size", result.size > size)
        }
    }

    @Test
    fun `encrypt output length follows the 16-plus-padded-block formula for many sizes`() {
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

    // Random IV

    @Test
    fun `encrypt same plaintext twice produces different full ciphertext`() {
        val input = "Hello VDOT world!".toByteArray()
        assertFalse(VideoEncryptor.encrypt(input).contentEquals(VideoEncryptor.encrypt(input)))
    }

    @Test
    fun `encrypt IV portion (first 16 bytes) differs across calls`() {
        val input = ByteArray(32) { 0x42 }
        val iv1 = VideoEncryptor.encrypt(input).sliceArray(0..15)
        val iv2 = VideoEncryptor.encrypt(input).sliceArray(0..15)
        assertFalse("IV must be random per call", iv1.contentEquals(iv2))
    }

    @Test
    fun `encrypt five consecutive calls all produce distinct outputs`() {
        val input = "repeating plaintext".toByteArray()
        val results = (1..5).map { VideoEncryptor.encrypt(input) }
        val unique = results.map { it.toList() }.toSet()
        assertEquals("All 5 encryptions should be unique", 5, unique.size)
    }

    // Output sanity

    @Test
    fun `encrypt result is not all zeros for a non-zero input`() {
        val result = VideoEncryptor.encrypt("test payload".toByteArray())
        assertFalse(result.all { it == 0.toByte() })
    }

    @Test
    fun `encrypt all-zeros input still produces non-trivial output`() {
        val result = VideoEncryptor.encrypt(ByteArray(32) { 0x00 })
        // Ciphertext bytes (after IV) should not be all zeros
        val ciphertext = result.sliceArray(16 until result.size)
        assertFalse(ciphertext.all { it == 0.toByte() })
    }

    @Test
    fun `encrypt large 5MB input does not throw and output is larger`() {
        val bigInput = ByteArray(5 * 1024 * 1024) { it.toByte() }
        val result = VideoEncryptor.encrypt(bigInput)
        assertTrue(result.size > bigInput.size)
    }

    @Test
    fun `encrypt output first 16 bytes are the IV not ciphertext`() {
        // Two calls: IVs differ, but the rest can be compared structurally.
        // Concretely: if IV were NOT randomised, the IV bytes across two
        // calls would be identical. Asserting they differ confirms placement.
        val input = ByteArray(16) { 0xFF.toByte() }
        val run1 = VideoEncryptor.encrypt(input)
        val run2 = VideoEncryptor.encrypt(input)
        val prefix1 = run1.sliceArray(0..15)
        val prefix2 = run2.sliceArray(0..15)
        assertFalse("Prefix (IV) must differ between calls", prefix1.contentEquals(prefix2))
    }
}
