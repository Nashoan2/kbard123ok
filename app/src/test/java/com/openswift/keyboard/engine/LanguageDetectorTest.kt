package com.openswift.keyboard.engine

import com.openswift.keyboard.data.KeyboardLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageDetectorTest {
    private val languages = listOf(
        KeyboardLanguage("en", "English", "en_US", "qwerty", 1),
        KeyboardLanguage("ar", "العربية (Arabic)", "ar", "arabic", 2)
    )

    private val frequencies = mapOf(
        "en" to mapOf("the" to 10000, "and" to 9000, "hello" to 3000),
        "ar" to mapOf("مرحبا" to 7500, "شكرا" to 7600, "السلام" to 7450)
    )

    private val detector = LanguageDetector(languages) { language, word ->
        frequencies[language]?.get(word) ?: 0
    }

    @Test
    fun detectsArabicFromDictionaryEvidence() {
        val result = detector.detect(listOf("مرحبا", "شكرا"), currentLanguage = "en")

        assertEquals("ar", result?.languageCode)
    }

    @Test
    fun detectsArabicFromAccentEvidence() {
        val result = detector.detect(listOf("مساء"), currentLanguage = "en")

        assertEquals("ar", result?.languageCode)
    }

    @Test
    fun keepsCurrentLanguageWhenEvidenceIsAmbiguous() {
        val result = detector.detect(listOf("hello"), currentLanguage = "en")

        assertNull(result)
    }
}
