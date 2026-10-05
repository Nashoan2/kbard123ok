package com.openswift.keyboard.data

import com.openswift.keyboard.R

data class KeyboardLanguage(
    val code: String,
    val name: String,
    val locale: String,
    val layoutId: String,
    val wordListRes: Int
)

object KeyboardLanguages {
    val English = KeyboardLanguage("en", "English", "en_US", "qwerty", R.raw.words)
    val Arabic = KeyboardLanguage("ar", "العربية (Arabic)", "ar", "arabic", R.raw.words_ar)

    val all = listOf(
        English,
        Arabic
    )

    fun byCode(code: String?): KeyboardLanguage {
        val normalized = code.orEmpty().lowercase().substringBefore('_').substringBefore('-')
        return all.firstOrNull { it.code == normalized } ?: English
    }

    fun byLocale(locale: String?): KeyboardLanguage = byCode(locale)
}
