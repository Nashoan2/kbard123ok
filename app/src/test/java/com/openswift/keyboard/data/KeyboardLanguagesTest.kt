package com.openswift.keyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLanguagesTest {
    @Test
    fun supportedLanguagesExposeExpectedLayoutDefaults() {
        val byCode = KeyboardLanguages.all.associateBy { it.code }

        assertEquals("qwerty", byCode.getValue("en").layoutId)
        assertEquals("arabic", byCode.getValue("ar").layoutId)
    }

    @Test
    fun localeLookupFallsBackToEnglishForUnknownLocales() {
        assertEquals("ar", KeyboardLanguages.byLocale("ar").code)
        assertEquals("ar", KeyboardLanguages.byLocale("ar_SA").code)
        assertEquals("ar", KeyboardLanguages.byLocale("ar-EG").code)
        assertEquals("en", KeyboardLanguages.byLocale("en_US").code)
        assertEquals("en", KeyboardLanguages.byLocale("ja_JP").code)
        assertEquals(listOf("en", "ar"), KeyboardLanguages.all.map { it.code })
    }
}
