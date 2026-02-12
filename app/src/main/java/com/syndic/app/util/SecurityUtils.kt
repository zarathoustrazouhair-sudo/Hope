package com.syndic.app.util

import java.security.MessageDigest

object SecurityUtils {

    fun hashPin(pin: String): String {
        if (pin.isBlank()) return ""
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun validatePin(inputPin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return false
        val inputHash = hashPin(inputPin)
        return inputHash == storedHash
    }
}
