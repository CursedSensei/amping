package com.pinghtdog.amping.ui.session

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object VideoEncryptor {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    
    // 16-byte symmetric key: "VDOTSecureKey202"
    private val keyBytes = byteArrayOf(
        0x56, 0x44, 0x4f, 0x54, 0x53, 0x65, 0x63, 0x75,
        0x72, 0x65, 0x4b, 0x65, 0x79, 0x32, 0x30, 0x32
    )

    fun encrypt(inputBytes: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance(ALGORITHM)
        
        // Generate a random 16-byte Initialization Vector (IV)
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encryptedData = cipher.doFinal(inputBytes)
        
        // Prepend the IV (first 16 bytes) to the encrypted payload so the decoder knows the exact IV!
        return iv + encryptedData
    }

//    fun decrypt(encryptedBytes: ByteArray): ByteArray {
//        if (encryptedBytes.size < 16) throw IllegalArgumentException("Ciphertext size is too small")
//
//        val iv = encryptedBytes.sliceArray(0..15)
//        val data = encryptedBytes.sliceArray(16 until encryptedBytes.size)
//
//        val keySpec = SecretKeySpec(keyBytes, "AES")
//        val cipher = Cipher.getInstance(ALGORITHM)
//        val ivSpec = IvParameterSpec(iv)
//
//        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
//        return cipher.doFinal(data)
//    }
}
