package com.openswift.keyboard.layout

import com.openswift.keyboard.layout.KeyCode as KC

private fun letter(c: Char, popup: List<String> = emptyList()) =
    Key(c.toString(), c.code, popup = popup)

object Layouts {

    /** Long-press popup alternates (mainly accents and Arabic variants). */
    private val popups: Map<Char, List<String>> = mapOf(
        'a' to listOf("á","à","â","ä","ã","å","æ","ā"),
        'e' to listOf("é","è","ê","ë","ē","ę"),
        'i' to listOf("í","ì","î","ï","ī"),
        'o' to listOf("ó","ò","ô","ö","õ","ø","œ","ō"),
        'u' to listOf("ú","ù","û","ü","ū"),
        'y' to listOf("ý","ÿ"),
        'n' to listOf("ñ","ń"),
        'c' to listOf("ç","ć","č"),
        's' to listOf("ß","ś","š"),
        'z' to listOf("ž","ź","ż"),
        'l' to listOf("ł"),
        'd' to listOf("đ"),
        'ا' to listOf("أ", "إ", "آ", "ء", "ٱ"),
        'و' to listOf("ؤ"),
        'ي' to listOf("ى", "ئ"),
        'ه' to listOf("ة"),
        'ت' to listOf("ة", "ث"),
        'ح' to listOf("خ", "ج"),
        'ر' to listOf("ز"),
        'د' to listOf("ذ"),
        'ص' to listOf("ض"),
        'ط' to listOf("ظ"),
        'ع' to listOf("غ"),
        'ف' to listOf("ڤ"),
        'ب' to listOf("پ"),
        'ك' to listOf("گ"),
        'ل' to listOf("لا", "لأ", "لإ", "لآ"),
        '@' to listOf("&"),
        '$' to listOf("€"),
        '(' to listOf("<"),
        ')' to listOf(">"),
        '-' to listOf("_"),
        '\'' to listOf("<"),
        '"' to listOf("«"),
        ':' to listOf("؛"),
        '#' to listOf("%"),
        '!' to listOf("¡"),
        '؟' to listOf("?"),
        ',' to listOf("،"),
        '.' to listOf("…"),
        '=' to listOf("≠"),
        '+' to listOf("±"),
        '*' to listOf("×"),
        '/' to listOf("÷"),
    )

    val Qwerty = KeyLayout(
        "qwerty",
        listOf(
            "qwertyuiop".map { letter(it, popups[it] ?: emptyList()) },
            buildList {
                add(Key("", KC.SPACER, widthWeight = 0.5f))
                addAll("asdfghjkl".map { letter(it, popups[it] ?: emptyList()) })
                add(Key("", KC.SPACER, widthWeight = 0.5f))
            },
            buildList {
                add(Key("Shift", KC.SHIFT, widthWeight = 1.5f, isModifier = true))
                addAll("zxcvbnm".map { letter(it, popups[it] ?: emptyList()) })
                add(Key("Delete", KC.DELETE, widthWeight = 1.5f, isModifier = true))
            },
            bottomRow()
        )
    )

    val Arabic = KeyLayout(
        "arabic",
        listOf(
            // Row 1 (11 keys): ض ص ث ق ف غ ع ه خ ح ج
            listOf(
                Key("ض", 'ض'.code),
                Key("ص", 'ص'.code),
                Key("ث", 'ث'.code),
                Key("ق", 'ق'.code),
                Key("ف", 'ف'.code),
                Key("غ", 'غ'.code),
                Key("ع", 'ع'.code),
                Key("ه", 'ه'.code),
                Key("خ", 'خ'.code),
                Key("ح", 'ح'.code),
                Key("ج", 'ج'.code)
            ),
            // Row 2 (11 keys): ش س ي ب ل ا ت ن م ك ة
            listOf(
                Key("ش", 'ش'.code),
                Key("س", 'س'.code),
                Key("ي", 'ي'.code),
                Key("ب", 'ب'.code),
                Key("ل", 'ل'.code),
                Key("ا", 'ا'.code),
                Key("ت", 'ت'.code),
                Key("ن", 'ن'.code),
                Key("م", 'م'.code),
                Key("ك", 'ك'.code),
                Key("ة", 'ة'.code)
            ),
            // Row 3 (10 keys): ى ظ ط ذ د ز ر و أ Delete
            listOf(
                Key("ى", 'ى'.code),
                Key("ظ", 'ظ'.code),
                Key("ط", 'ط'.code),
                Key("ذ", 'ذ'.code),
                Key("د", 'د'.code),
                Key("ز", 'ز'.code),
                Key("ر", 'ر'.code),
                Key("و", 'و'.code),
                Key("أ", 'أ'.code),
                Key("Delete", KC.DELETE, widthWeight = 1.55f, isModifier = true)
            ),
            // Row 4 (6 keys): 123, AR, ء, space, لا, Enter
            listOf(
                Key("123", KC.SYMBOLS, widthWeight = 1.35f, isModifier = true),
                Key("AR", KC.LANGUAGE, widthWeight = 1.15f, isModifier = true),
                Key("ء", 'ء'.code, widthWeight = 0.95f),
                Key("space", KC.SPACE, widthWeight = 4.1f),
                Key("لا", 'ل'.code, widthWeight = 1.0f),
                Key("Enter", KC.ENTER, widthWeight = 1.45f, isModifier = true)
            )
        )
    )

    val Symbols = KeyLayout(
        "symbols",
        listOf(
            listOf(
                letter('1'), letter('2'), letter('3'), letter('4'), letter('5'),
                letter('6'), letter('7'), letter('8'), letter('9'), letter('0')
            ),
            listOf(
                letter('@', listOf("&")),
                letter('#', listOf("%")),
                letter('$', listOf("€")),
                letter('_'),
                letter('&'),
                letter('-', listOf("—")),
                letter('+'),
                letter('(', listOf("<")),
                letter(')', listOf(">")),
                letter('/')
            ),
            buildList {
                addAll(
                    listOf(
                        letter('='),
                        letter('\\'),
                        letter('*'),
                        letter('"'),
                        letter('\''),
                        letter(':'),
                        letter(';'),
                        letter('!'),
                        letter('؟')
                    )
                )
                add(Key("Delete", KC.DELETE, widthWeight = 1.4f, isModifier = true))
            },
            listOf(
                Key("ABC", KC.ABC, widthWeight = 1.35f, isModifier = true),
                Key("AR", KC.LANGUAGE, widthWeight = 1.15f, isModifier = true),
                Key("،", KC.COMMA, widthWeight = 1.0f),
                Key("space", KC.SPACE, widthWeight = 3.9f),
                Key(".", KC.PERIOD, widthWeight = 0.9f),
                Key("Enter", KC.ENTER, widthWeight = 1.45f, isModifier = true)
            )
        )
    )

    val SymbolsShift = KeyLayout(
        "symbols2",
        listOf(
            "~`|•√π÷×§∆".map { letter(it) },
            "£¥€°^{}\\©®".map { letter(it) },
            buildList {
                add(Key("1/2", KC.SHIFT_SYMBOLS, widthWeight = 1.35f, isModifier = true))
                addAll("%®™✓[]".map { letter(it) })
                add(Key("Delete", KC.DELETE, widthWeight = 1.35f, isModifier = true))
            },
            listOf(
                Key("ABC", KC.ABC, widthWeight = 1.35f, isModifier = true),
                letter('0'),
                letter('.', popups['.'] ?: emptyList()),
                Key("space", KC.SPACE, widthWeight = 3.6f),
                Key("2/2", KC.SHIFT_SYMBOLS, widthWeight = 1.2f, isModifier = true),
                Key("Enter", KC.ENTER, widthWeight = 1.4f, isModifier = true)
            )
        )
    )

    val Numpad = KeyLayout(
        "numpad",
        listOf(
            listOf(
                Key("+", '+'.code, widthWeight = 1.15f),
                Key("-", '-'.code, widthWeight = 1.15f),
                Key("*", '*'.code, widthWeight = 1.15f),
                Key("/", '/'.code, widthWeight = 1.15f)
            ),
            listOf(
                Key("1", '1'.code, widthWeight = 2.0f),
                Key("2", '2'.code, widthWeight = 2.0f),
                Key("3", '3'.code, widthWeight = 2.0f),
                Key("%", '%'.code, widthWeight = 1.15f)
            ),
            listOf(
                Key("4", '4'.code, widthWeight = 2.0f),
                Key("5", '5'.code, widthWeight = 2.0f),
                Key("6", '6'.code, widthWeight = 2.0f),
                Key("␣", KC.SPACE, widthWeight = 1.15f)
            ),
            listOf(
                Key("7", '7'.code, widthWeight = 2.0f),
                Key("8", '8'.code, widthWeight = 2.0f),
                Key("9", '9'.code, widthWeight = 2.0f),
                Key("Delete", KC.DELETE, widthWeight = 1.15f, isModifier = true)
            ),
            listOf(
                Key("ABC", KC.ABC, widthWeight = 1.15f, isModifier = true),
                Key(",", ','.code, widthWeight = 0.9f),
                Key("!?#", KC.SHIFT_SYMBOLS, widthWeight = 1.1f, isModifier = true),
                Key("0", '0'.code, widthWeight = 2.0f),
                Key("=", '='.code, widthWeight = 1.1f),
                Key(".", '.'.code, widthWeight = 0.9f),
                Key("Enter", KC.ENTER, widthWeight = 1.15f, isModifier = true)
            )
        )
    )

    private fun bottomRow(symbols: Boolean = false): List<Key> = listOf(
        Key(if (symbols) "abc" else "123", if (symbols) KC.ABC else KC.SYMBOLS, widthWeight = 1.35f, isModifier = true),
        Key("EN", KC.LANGUAGE, widthWeight = 1.15f, isModifier = true),
        Key(",", KC.COMMA, widthWeight = 1.0f, popup = listOf("?", "!")),
        Key("space", KC.SPACE, widthWeight = 3.9f),
        Key(".", KC.PERIOD, widthWeight = 0.9f, popup = listOf("…")),
        Key("Enter", KC.ENTER, widthWeight = 1.45f, isModifier = true)
    )

    // Preserved for contract test compatibility
    val legacyEmojiKey = Key("Emoji", KC.EMOJI, isModifier = true)
    val legacySettingsKey = Key("Settings", KC.SETTINGS, isModifier = true)

    fun byId(id: String): KeyLayout = when (id) {
        "numpad" -> Numpad
        "arabic" -> Arabic
        else -> Qwerty
    }
}
