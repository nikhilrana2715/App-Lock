package com.applock.protectedapps.data.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Constant-time byte array comparison to prevent timing attacks during PIN verification.
     */
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b.toInt() and 0xFF))
        }
        return sb.toString()
    }

    fun sanitizeLegacyHex(hex: String): String {
        if (hex.isBlank()) return hex
        return hex.replace("ffffff", "")
    }

    fun hexToBytes(hex: String): ByteArray {
        val cleanHex = sanitizeLegacyHex(hex)
        if (cleanHex.length % 2 != 0) return ByteArray(0)
        val len = cleanHex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val high = Character.digit(cleanHex[i], 16)
            val low = Character.digit(cleanHex[i + 1], 16)
            if (high == -1 || low == -1) return ByteArray(0)
            data[i / 2] = ((high shl 4) + low).toByte()
            i += 2
        }
        return data
    }
}
