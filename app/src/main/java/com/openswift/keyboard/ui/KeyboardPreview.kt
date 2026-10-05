package com.openswift.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openswift.keyboard.R
import com.openswift.keyboard.theme.KbTheme

@Composable
fun KeyboardPreview(
    theme: KbTheme,
    modifier: Modifier = Modifier,
    layoutId: String = "arabic"
) {
    val bgColor = Color(theme.keyBackground)
    val modifierBg = Color(theme.keyModifierBackground)
    val textColor = Color(theme.keyText)
    val accentColor = Color(theme.keyAccent)
    val subtleColor = Color(theme.suggestionText)
    val keySpacing = 3.dp
    val keyHeight = 38.dp
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(theme.background), RoundedCornerShape(14.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(keySpacing)
    ) {
        // Top Toolbar Strip matching the screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_audio_wave),
                contentDescription = "Wave",
                tint = subtleColor,
                modifier = Modifier.size(17.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_sentiment_satisfied),
                contentDescription = "Emoji",
                tint = subtleColor,
                modifier = Modifier.size(17.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_text_cursor),
                contentDescription = "Cursor",
                tint = subtleColor,
                modifier = Modifier.size(17.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_content_paste),
                contentDescription = "Clipboard",
                tint = subtleColor,
                modifier = Modifier.size(17.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                contentDescription = "Hide",
                tint = subtleColor,
                modifier = Modifier.size(17.dp)
            )
        }

        when (layoutId) {
            "arabic" -> {
                // Row 1 (11 keys): ض ص ث ق ف غ ع ه خ ح ج
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val row1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج")
                    row1.forEach { char ->
                        PreviewKey(char, bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
                    }
                }
                
                // Row 2 (11 keys): ش س ي ب ل ا ت ن م ك ة
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val row2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ة")
                    row2.forEach { char ->
                        PreviewKey(char, bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
                    }
                }
                
                // Row 3 (10 keys): ى ظ ط ذ د ز ر و أ Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val row3 = listOf("ى", "ظ", "ط", "ذ", "د", "ز", "ر", "و", "أ")
                    row3.forEach { char ->
                        PreviewKey(char, bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
                    }
                    PreviewKey("⌫", modifierBg, accentColor, keyHeight, modifier = Modifier.weight(1.55f), isModifier = true)
                }
                
                // Row 4 (6 keys): 123, AR, ء, space, لا, Enter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviewKey("123", modifierBg, textColor, keyHeight, modifier = Modifier.weight(1.35f), isModifier = true)
                    PreviewKey("AR", modifierBg, accentColor, keyHeight, modifier = Modifier.weight(1.15f), isModifier = true)
                    PreviewKey("ء", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.95f))
                    PreviewSpaceKey(bgColor, keyHeight, modifier = Modifier.weight(4.1f))
                    PreviewKey("لا", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.0f))
                    PreviewKey("↵", modifierBg, accentColor, keyHeight, modifier = Modifier.weight(1.45f), isModifier = true)
                }
            }
            "123", "numpad" -> {
                // Layout matching 123.png exactly
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight * 4 + keySpacing * 3),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing)
                ) {
                    // Column 1: Operators (+, -, *, /) and ABC at bottom
                    Column(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(keySpacing)
                    ) {
                        PreviewKey("+", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.75f))
                        PreviewKey("-", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.75f))
                        PreviewKey("*", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.75f))
                        PreviewKey("/", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.75f))
                        PreviewKey("ABC", modifierBg, textColor, keyHeight, modifier = Modifier.weight(1f), isModifier = true, isPill = true)
                    }

                    // Columns 2, 3, 4, 5
                    Column(
                        modifier = Modifier
                            .weight(7.15f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(keySpacing)
                    ) {
                        // Row 1: 1 2 3 %
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(keySpacing)
                        ) {
                            PreviewKey("1", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("2", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("3", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("%", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.15f))
                        }
                        // Row 2: 4 5 6 ␣
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(keySpacing)
                        ) {
                            PreviewKey("4", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("5", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("6", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("␣", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.15f))
                        }
                        // Row 3: 7 8 9 ⌫
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(keySpacing)
                        ) {
                            PreviewKey("7", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("8", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("9", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("⌫", modifierBg, textColor, keyHeight, modifier = Modifier.weight(1.15f), isModifier = true)
                        }
                        // Row 4: , !?# 0 = . ↵
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(keySpacing)
                        ) {
                            PreviewKey(",", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.9f))
                            PreviewKey("!?#", modifierBg, textColor, keyHeight, modifier = Modifier.weight(1.1f), isModifier = true)
                            PreviewKey("0", bgColor, textColor, keyHeight, modifier = Modifier.weight(2f))
                            PreviewKey("=", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.1f))
                            PreviewKey(".", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.9f))
                            PreviewKey("↵", Color(0xFFA8C7FA), Color(0xFF041E49), keyHeight, modifier = Modifier.weight(1.15f), isModifier = true, isPill = true)
                        }
                    }
                }
            }
            else -> {
                // Row 1: qwerty...
                Row(
                    modifier = Modifier.fillMaxWidth().height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").forEachIndexed { index, c ->
                        val hint = if (index == 9) "0" else (index + 1).toString()
                        PreviewKey(c, bgColor, textColor, keyHeight, modifier = Modifier.weight(1f), hint = hint)
                    }
                }
                
                // Row 2: asdf...
                Row(
                    modifier = Modifier.fillMaxWidth().height(keyHeight).padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").forEach {
                        PreviewKey(it, bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
                    }
                }
                
                // Row 3: shift zxcv... delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviewKey("⇧", modifierBg, textColor, keyHeight, modifier = Modifier.weight(1.3f), isModifier = true)
                    listOf("z", "x", "c", "v", "b", "n", "m").forEach {
                        PreviewKey(it, bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
                    }
                    PreviewKey("⌫", modifierBg, accentColor, keyHeight, modifier = Modifier.weight(1.3f), isModifier = true)
                }
                
                // Row 4: Space bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviewKey("123", modifierBg, textColor, keyHeight, modifier = Modifier.weight(1.35f), isModifier = true)
                    PreviewKey("EN", modifierBg, accentColor, keyHeight, modifier = Modifier.weight(1.15f), isModifier = true)
                    PreviewKey(",", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.0f))
                    PreviewSpaceKey(bgColor, keyHeight, modifier = Modifier.weight(3.9f))
                    PreviewKey(".", bgColor, textColor, keyHeight, modifier = Modifier.weight(0.9f))
                    PreviewKey("↵", modifierBg, accentColor, keyHeight, modifier = Modifier.weight(1.45f), isModifier = true)
                }
            }
        }
    }
}

@Composable
fun PreviewSpaceKey(
    bgColor: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {}
}

@Composable
fun PreviewKey(
    label: String,
    bgColor: Color,
    textColor: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    isModifier: Boolean = false,
    hint: String? = null,
    isPill: Boolean = false
) {
    val cornerRadius = if (isPill) 20.dp else 6.dp
    Box(
        modifier = modifier
            .height(height)
            .background(
                bgColor,
                RoundedCornerShape(cornerRadius)
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (label == "⌫") {
            Icon(
                painter = painterResource(R.drawable.ic_backspace),
                contentDescription = "Delete",
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        } else if (label == "↵") {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_return),
                contentDescription = "Enter",
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        } else if (hint != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Text(
                    label,
                    color = textColor,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    hint,
                    color = Color(0xFF8E95A5),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        } else {
            Text(
                label,
                color = textColor,
                fontSize = if (label.length > 2) 11.sp else 14.sp,
                fontWeight = if (label in listOf("123", "AR", "EN", "ABC")) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
