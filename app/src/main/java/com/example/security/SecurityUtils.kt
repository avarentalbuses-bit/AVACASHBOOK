package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale

object SecurityUtils {
    private val secureRandom = SecureRandom()

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashWithSalt(input: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:$input"
        val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(input: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashWithSalt(input, salt)
        return calculated.equals(expectedHash, ignoreCase = true)
    }

    fun computeTransactionChecksum(
        id: Long,
        registerType: String,
        date: String,
        cashIn: Double,
        cashOut: Double,
        amount: Double,
        description: String,
        reference: String
    ): String {
        val md = MessageDigest.getInstance("SHA-256")
        val raw = String.format(
            Locale.US,
            "ID:%d|TYPE:%s|DATE:%s|IN:%.2f|OUT:%.2f|AMT:%.2f|DESC:%s|REF:%s",
            id, registerType, date, cashIn, cashOut, amount, description.trim(), reference.trim()
        )
        val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }
}
