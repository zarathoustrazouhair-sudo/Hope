package com.syndic.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun `hashPin generates correct SHA-256 hash`() {
        val pin = "1234"
        // SHA-256 for "1234"
        val expectedHash = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"

        val actualHash = SecurityUtils.hashPin(pin)

        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun `hashPin returns empty string for empty input`() {
        assertEquals("", SecurityUtils.hashPin(""))
        assertEquals("", SecurityUtils.hashPin("   "))
    }

    @Test
    fun `validatePin returns true for matching pin`() {
        val pin = "5678"
        val hash = SecurityUtils.hashPin(pin)

        assertTrue(SecurityUtils.validatePin(pin, hash))
    }

    @Test
    fun `validatePin returns false for incorrect pin`() {
        val correctPin = "9999"
        val hash = SecurityUtils.hashPin(correctPin)
        val wrongPin = "8888"

        assertFalse(SecurityUtils.validatePin(wrongPin, hash))
    }
}
