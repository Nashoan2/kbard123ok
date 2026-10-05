package com.openswift.keyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.openswift.keyboard.R
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.engine.GlideDecoder
import com.openswift.keyboard.engine.WordList
import com.openswift.keyboard.layout.Key
import com.openswift.keyboard.layout.KeyLayout
import com.openswift.keyboard.layout.KeyCode as KC
import com.openswift.keyboard.theme.KbTheme
import com.openswift.keyboard.theme.Themes
import kotlin.math.ceil

class KeyboardView(
    ctx: Context,
    private val settings: Settings,
    private var wordList: WordList,
    private var userDict: UserDictionary,
    private var keyLayout: KeyLayout,
    private val theme: KbTheme = Themes.byId(settings.theme),
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(ctx, attrs, defStyle) {

    private var onKeyListener: ((Int, String) -> Unit)? = null
    private var onGlideListener: ((String) -> Unit)? = null
    private var suggestions: List<String> = emptyList()
    private var shiftActive = false
    private var predictionEnabled = true
    private var glideEnabled = settings.glideEnabled
    private var keyHeightDp = settings.keyHeightDp
    private var glideDecoder = GlideDecoder(wordList, userDict)
    
    // Compute layout with number row if enabled
    private var effectiveLayout: KeyLayout = if (keyLayout.id == "numpad" || keyLayout.id.startsWith("symbols")) {
        keyLayout
    } else if (settings.numberRow) {
        // Prepend number row to the layout
        val numberRow = (1..10).map { i ->
            val ch = if (i == 10) '0' else (i + 48).toChar()
            Key(ch.toString(), ch.code)
        }
        KeyLayout(
            keyLayout.id + "_with_numbers",
            listOf(numberRow) + keyLayout.rows
        )
    } else {
        keyLayout
    }

    private val keyBounds = mutableMapOf<Key, Rect>()
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 18f
    }
    private val keyOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = theme.keyAccent
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = theme.keyText
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    }
    private val suggestionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f
        color = theme.suggestionText
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = 0xFF8A909D.toInt()
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    }
    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = android.graphics.Color.WHITE
        alpha = 14
    }
    
    // Additional Paint objects (allocated once, reused per frame)
    private val suggestionBgPaint = Paint().apply { color = theme.suggestionBg }
    private val pillPaint = Paint().apply { 
        style = Paint.Style.FILL
        color = theme.keyBackground
    }
    private val pillBorderPaint = Paint().apply { 
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = theme.keyAccent
        alpha = 90
    }
    private val previewPaint = Paint().apply {
        textSize = 10f
        color = theme.suggestionText
        alpha = 180
    }
    private val trailPaint = Paint().apply {
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = theme.gestureTrail
    }
    private val ripplePaint = Paint().apply {
        style = Paint.Style.FILL
        color = theme.keyAccent
    }
    private val keyModifierBgPaint = Paint().apply { color = theme.keyModifierBackground }
    private val keyBgPaint = Paint().apply { color = theme.keyBackground }
    private val shiftHighlightPaint = Paint().apply {
        color = theme.keyAccent
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val clipboardIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_content_paste)
        ?.mutate()
        ?.apply { setTint(theme.keyText) }
    private val shiftIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_keyboard_capslock)?.mutate()
    private val deleteIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_backspace)?.mutate()
    private val enterIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_keyboard_return)?.mutate()
    private val emojiIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_sentiment_satisfied)?.mutate()
    private val languageIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_language)?.mutate()
    private val settingsIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_settings)?.mutate()
    private val micIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_mic)?.mutate()
    private val audioWaveIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_audio_wave)?.mutate()
    private val textCursorIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_text_cursor)?.mutate()
    private val hideIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_keyboard_arrow_down)?.mutate()
    private val hubMonogramIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_hub_monogram)?.mutate()
    private val numpadEnterIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_numpad_return)?.mutate()
    private val toolbarBounds = mutableMapOf<String, Rect>()

    var onHideKeyboard: (() -> Unit)? = null
    var onOpenHub: (() -> Unit)? = null
    var onOpenTextEditing: (() -> Unit)? = null
    var onOpenClipboard: (() -> Unit)? = null
    var onHeightChanged: ((Int) -> Unit)? = null

    var isResizeMode = false
        set(value) {
            field = value
            invalidate()
        }

    private val resizeMinusBounds = Rect()
    private val resizePlusBounds = Rect()
    private val resizeDoneBounds = Rect()

    private var isGliding = false
    private var glideStartTime = 0L
    private val glideSamples = mutableListOf<GlideDecoder.Sample>()
    private val suggestionBounds = mutableMapOf<String, Rect>()
    
    // Ripple effect tracking
    private data class Ripple(val x: Float, val y: Float, val startTime: Long)
    private val ripples = mutableListOf<Ripple>()
    private val rippleAnimDuration = 400L
    private val maxRipples = 20 // Cap concurrent ripples to prevent memory bloat
    
    // Glide trail gradient
    private data class TrailPoint(val x: Float, val y: Float, val time: Long)
    private val glideTrail = mutableListOf<TrailPoint>()
    private val trailFadeMs = 300L

    private var longPressTriggered = false
    private var touchDownX = 0f
    private var touchDownY = 0f
    private val longPressHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        
        // Mirror the drawing path exactly so the final row is never clipped.
        val keyHeightPx = keyHeightDp * density
        val numRows = if (effectiveLayout.id == "numpad") 4 else effectiveLayout.rows.size // includes number row if enabled
        val rowSpacingPx = 2f * density
        val suggestionHeight = if (predictionEnabled) {
            (4f * density) + (keyHeightPx * 0.82f) + (4f * density)
        } else {
            0f
        }
        val rowsHeight = (keyHeightPx * numRows) +
            (rowSpacingPx * (numRows - 1).coerceAtLeast(0))
        val h = ceil(suggestionHeight + rowsHeight).toInt()
        
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.background)

        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        
        // Calculate key height in pixels
        val keyHeightPx = keyHeightDp * density
        val suggestionHeightPx = keyHeightPx * 0.82f
        val rowSpacingPx = 2f * density // small gap between rows
        
        // Scale typography with the user's selected key height.
        textPaint.textSize = keyHeightPx * 0.50f
        suggestionPaint.textSize = keyHeightPx * 0.32f
        previewPaint.textSize = (keyHeightPx * 0.18f)
        
        // Draw suggestions row as pills with preview or toolbar icons
        var y = 0f
        suggestionBounds.clear()
        toolbarBounds.clear()

        if (predictionEnabled) {
            y = 4f * density // small top padding
            canvas.drawRect(0f, 0f, w, y + suggestionHeightPx, suggestionBgPaint)

            val showWordSuggestions = false
            if (showWordSuggestions && suggestions.isNotEmpty()) {
                val outerPadding = 10f * density
                val pillGap = 8f * density
                val pillWidth = (w - (outerPadding * 2f) - (pillGap * 2f)) / 3f

                suggestions.take(3).forEachIndexed { index, sugg ->
                    val x = outerPadding + (index * (pillWidth + pillGap))
                    val pillHeight = suggestionHeightPx - (8f * density)
                    val pillRadius = minOf(12f * density, pillHeight / 2f)
                    val pillY = y + (4f * density)

                    val rect = Rect(
                        x.toInt(),
                        pillY.toInt(),
                        (x + pillWidth).toInt(),
                        (pillY + pillHeight).toInt(),
                    )
                    suggestionBounds[sugg] = rect

                    canvas.drawRoundRect(
                        x, pillY, x + pillWidth, pillY + pillHeight,
                        pillRadius, pillRadius,
                        pillPaint
                    )

                    canvas.drawRoundRect(
                        x, pillY, x + pillWidth, pillY + pillHeight,
                        pillRadius, pillRadius,
                        pillBorderPaint
                    )

                    val textY = pillY + (pillHeight / 2f) -
                        ((suggestionPaint.ascent() + suggestionPaint.descent()) / 2f)
                    canvas.drawText(sugg, x + pillWidth / 2f, textY, suggestionPaint)
                }
            } else if (isResizeMode) {
                val btnHeight = suggestionHeightPx - (8f * density)
                val btnTop = y + (4f * density)
                val btnBottom = btnTop + btnHeight
                val btnMargin = 8f * density

                // Done button on left [ تم ✓ ]
                val doneWidth = 60f * density
                resizeDoneBounds.set(btnMargin.toInt(), btnTop.toInt(), (btnMargin + doneWidth).toInt(), btnBottom.toInt())
                canvas.drawRoundRect(btnMargin, btnTop, btnMargin + doneWidth, btnBottom, 6f * density, 6f * density, pillPaint)
                canvas.drawRoundRect(btnMargin, btnTop, btnMargin + doneWidth, btnBottom, 6f * density, 6f * density, pillBorderPaint)
                val doneTextY = resizeDoneBounds.centerY() - ((suggestionPaint.ascent() + suggestionPaint.descent()) / 2f)
                canvas.drawText("تم ✓", resizeDoneBounds.centerX().toFloat(), doneTextY, suggestionPaint)

                // Plus button on right [ + ]
                val stepBtnWidth = 42f * density
                val plusLeft = w - btnMargin - stepBtnWidth
                resizePlusBounds.set(plusLeft.toInt(), btnTop.toInt(), (plusLeft + stepBtnWidth).toInt(), btnBottom.toInt())
                canvas.drawRoundRect(plusLeft, btnTop, plusLeft + stepBtnWidth, btnBottom, 6f * density, 6f * density, pillPaint)
                canvas.drawRoundRect(plusLeft, btnTop, plusLeft + stepBtnWidth, btnBottom, 6f * density, 6f * density, pillBorderPaint)
                val plusTextY = resizePlusBounds.centerY() - ((textPaint.ascent() + textPaint.descent()) / 2f)
                canvas.drawText("+", resizePlusBounds.centerX().toFloat(), plusTextY, textPaint)

                // Minus button next to plus [ - ]
                val minusLeft = plusLeft - btnMargin - stepBtnWidth
                resizeMinusBounds.set(minusLeft.toInt(), btnTop.toInt(), (minusLeft + stepBtnWidth).toInt(), btnBottom.toInt())
                canvas.drawRoundRect(minusLeft, btnTop, minusLeft + stepBtnWidth, btnBottom, 6f * density, 6f * density, pillPaint)
                canvas.drawRoundRect(minusLeft, btnTop, minusLeft + stepBtnWidth, btnBottom, 6f * density, 6f * density, pillBorderPaint)
                val minusTextY = resizeMinusBounds.centerY() - ((textPaint.ascent() + textPaint.descent()) / 2f)
                canvas.drawText("-", resizeMinusBounds.centerX().toFloat(), minusTextY, textPaint)

                // Center indicator label
                val labelLeft = btnMargin + doneWidth + (4f * density)
                val labelRight = minusLeft - (4f * density)
                val labelCenter = (labelLeft + labelRight) / 2f
                val heightDesc = when {
                    keyHeightDp <= 58 -> "صغير"
                    keyHeightDp <= 68 -> "متوسط"
                    keyHeightDp <= 78 -> "مرتفع"
                    keyHeightDp <= 88 -> "كبير"
                    else -> "كبير جداً"
                }
                val labelText = "الارتفاع: $heightDesc ($keyHeightDp dp)"
                val labelY = resizeDoneBounds.centerY() - ((suggestionPaint.ascent() + suggestionPaint.descent()) / 2f)
                canvas.drawText(labelText, labelCenter, labelY, suggestionPaint)
            } else {
                // 5 toolbar icons: Monogram Hub, Smiley, Cursor, Clipboard, Hide
                val icons = listOf(
                    "hub" to (hubMonogramIcon ?: audioWaveIcon ?: micIcon),
                    "emoji" to (emojiIcon ?: languageIcon),
                    "cursor" to textCursorIcon,
                    "clipboard" to clipboardIcon,
                    "hide" to hideIcon,
                )
                val iconCount = icons.size
                val iconSlotWidth = w / iconCount.toFloat()
                val iconSize = (suggestionHeightPx * 0.44f).toInt()
                val iconTop = (y + ((suggestionHeightPx - iconSize) / 2f)).toInt()

                icons.forEachIndexed { index, (action, drawable) ->
                    if (drawable != null) {
                        val centerX = (index * iconSlotWidth) + (iconSlotWidth / 2f)
                        val iconLeft = (centerX - (iconSize / 2f)).toInt()
                        val hitRect = Rect(
                            (index * iconSlotWidth).toInt(),
                            y.toInt(),
                            ((index + 1) * iconSlotWidth).toInt(),
                            (y + suggestionHeightPx).toInt()
                        )
                        toolbarBounds[action] = hitRect
                        drawable.setTint(theme.suggestionText)
                        drawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                        drawable.draw(canvas)
                    }
                }
            }

            y += suggestionHeightPx + (4f * density)
        }

        // Draw keyboard rows
        keyBounds.clear()
        val keyPadding = 2.5f * density
        val keyCornerRadius = 6f * density

        if (effectiveLayout.id == "numpad") {
            drawNumpad(canvas, w, y, density, keyHeightPx, rowSpacingPx)
        } else {
            for (row in effectiveLayout.rows) {
                val totalWeight = row.sumOf { it.widthWeight.toDouble() }
                var x2 = 0f
                for (key in row) {
                    val kw = (w.toDouble() / totalWeight) * key.widthWeight.toDouble()
                    if (key.code == KC.SPACER) {
                        x2 += kw.toFloat()
                        continue
                    }
                    // Store bounds with padding applied (actual tappable area)
                    val rect = Rect(
                        (x2 + keyPadding).toInt(), 
                        (y + keyPadding).toInt(), 
                        (x2 + kw.toFloat() - keyPadding).toInt(), 
                        (y + keyHeightPx - keyPadding).toInt()
                    )
                    keyBounds[key] = rect

                    val bgColor = if (key.isModifier) theme.keyModifierBackground else theme.keyBackground
                    val bgPaint = if (key.isModifier) keyModifierBgPaint else keyBgPaint
                    bgPaint.color = bgColor
                    
                    canvas.drawRoundRect(
                        x2 + keyPadding, y + keyPadding, x2 + kw.toFloat() - keyPadding, y + keyHeightPx - keyPadding,
                        keyCornerRadius, keyCornerRadius,
                        bgPaint
                    )
                    
                    // Subtle modern top highlight for clean key separation
                    canvas.drawRoundRect(
                        x2 + keyPadding, y + keyPadding, x2 + kw.toFloat() - keyPadding, y + keyHeightPx - keyPadding,
                        keyCornerRadius, keyCornerRadius,
                        keyBorderPaint
                    )
                    
                    if (shiftActive && key.code == KC.SHIFT) {
                        canvas.drawRoundRect(
                            x2 + keyPadding, y + keyPadding, x2 + kw.toFloat() - keyPadding, y + keyHeightPx - keyPadding,
                            keyCornerRadius, keyCornerRadius,
                            shiftHighlightPaint
                        )
                    }
                    
                    // Draw key text (centered both horizontally and vertically)
                    val textX = x2 + kw.toFloat() / 2f
                    
                    // Convert to uppercase if shift is active and key is a letter
                    val displayLabel = if (key.code == KC.SPACE) {
                        ""
                    } else if (shiftActive && key.label.length == 1 && key.label[0].isLetter()) {
                        key.label.uppercase()
                    } else {
                        key.label
                    }
                    
                    val defaultTextSize = textPaint.textSize
                    val availableTextWidth = (kw.toFloat() - (keyPadding * 2) - (8f * density)).coerceAtLeast(1f)
                    val labelWidth = textPaint.measureText(displayLabel)
                    if (labelWidth > availableTextWidth) {
                        textPaint.textSize = defaultTextSize * (availableTextWidth / labelWidth)
                    }
                    val keyIcon = when {
                        key.code == KC.ENTER && key.label == "تنفيذ" -> null
                        key.code == KC.CLIPBOARD -> clipboardIcon
                        key.code == KC.SHIFT -> shiftIcon
                        key.code == KC.DELETE -> deleteIcon
                        key.code == KC.ENTER -> enterIcon
                        key.code == KC.SETTINGS -> settingsIcon
                        else -> null
                    }
                    if (keyIcon != null) {
                        val iconSize = (keyHeightPx * 0.44f).toInt()
                        val iconLeft = (textX - (iconSize / 2f)).toInt()
                        val iconTop = (y + ((keyHeightPx - iconSize) / 2f)).toInt()
                        val iconTint = when (key.code) {
                            KC.DELETE -> theme.keyAccent
                            KC.ENTER -> theme.keyAccent
                            KC.SHIFT -> if (shiftActive) theme.keyAccent else theme.keyText
                            else -> theme.keyText
                        }
                        keyIcon.setTint(iconTint)
                        keyIcon.setBounds(
                            iconLeft,
                            iconTop,
                            iconLeft + iconSize,
                            iconTop + iconSize,
                        )
                        keyIcon.draw(canvas)
                    } else {
                        val isCyanKey = displayLabel == "AR" || displayLabel == "EN" || key.label == "AR" || key.label == "EN"
                        val isBoldKey = isCyanKey || displayLabel == "123" || key.label == "123" || key.code in listOf(KC.ABC, KC.SYMBOLS, KC.SHIFT_SYMBOLS) || displayLabel == "ABC"

                        textPaint.color = if (isCyanKey) theme.keyAccent else theme.keyText
                        textPaint.typeface = if (isBoldKey) {
                            android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        } else {
                            android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                        }

                        val hasHint = key.popup.isNotEmpty() && !key.isModifier && key.code != KC.SPACE && (key.popup.firstOrNull()?.length ?: 0) <= 2
                        val hint = if (hasHint) key.popup.firstOrNull() else null

                        if (hasHint && hint != null) {
                            // Main symbol in upper portion
                            val mainY = y + (keyHeightPx * 0.38f) - ((textPaint.ascent() + textPaint.descent()) / 2f)
                            canvas.drawText(displayLabel, textX, mainY, textPaint)

                            // Secondary hint centered horizontally in lower portion
                            hintPaint.textSize = keyHeightPx * 0.28f
                            hintPaint.color = 0xFF8E95A5.toInt()
                            val hintY = y + (keyHeightPx * 0.74f) - ((hintPaint.ascent() + hintPaint.descent()) / 2f)
                            canvas.drawText(hint, textX, hintY, hintPaint)
                        } else {
                            // Perfectly vertically centered
                            val mainY = y + (keyHeightPx / 2f) - ((textPaint.ascent() + textPaint.descent()) / 2f)
                            canvas.drawText(displayLabel, textX, mainY, textPaint)
                        }
                        textPaint.color = theme.keyText
                        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                    }
                    textPaint.textSize = defaultTextSize

                    x2 += kw.toFloat()
                }
                y += keyHeightPx + rowSpacingPx
            }
        }

        // Draw glide trail with fade gradient (skip if reduced motion enabled)
        if (!settings.reducedMotion && glideTrail.isNotEmpty()) {
            val now = System.currentTimeMillis()
            for (i in 0 until glideTrail.size - 1) {
                val p1 = glideTrail[i]
                val p2 = glideTrail[i + 1]
                val age = (now - p1.time).toFloat().coerceAtLeast(0f)
                val progress = (age / trailFadeMs).coerceIn(0f, 1f)
                val alpha = ((1f - progress) * 255).toInt()
                trailPaint.color = theme.gestureTrail
                trailPaint.alpha = alpha
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, trailPaint)
            }
        }

        // Draw ripples (skip if reduced motion enabled)
        if (!settings.reducedMotion) {
            val now = System.currentTimeMillis()
            val expiredIndices = mutableListOf<Int>()
            for ((idx, ripple) in ripples.withIndex()) {
                val elapsed = now - ripple.startTime
                val progress = (elapsed.toFloat() / rippleAnimDuration).coerceIn(0f, 1f)
                
                if (progress >= 1f) {
                    expiredIndices.add(idx)
                    continue
                }
                
                val radius = (2f + (48f * progress)) * density
                val alpha = ((1f - progress) * 255).toInt()
                ripplePaint.color = theme.keyAccent
                ripplePaint.alpha = alpha
                canvas.drawCircle(ripple.x, ripple.y, radius, ripplePaint)
            }
            
            for (idx in expiredIndices.reversed()) {
                ripples.removeAt(idx)
            }
        } else {
            ripples.clear()
        }
        
        if (ripples.isNotEmpty() || glideTrail.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val density = resources.displayMetrics.density
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                glideStartTime = System.currentTimeMillis()
                isGliding = false
                glideSamples.clear()
                glideTrail.clear()
                touchDownX = event.x
                touchDownY = event.y
                longPressTriggered = false

                val pressedKey = findKeyAt(event.x, event.y)
                if (pressedKey != null && pressedKey.popup.isNotEmpty() && !pressedKey.isModifier) {
                    val popupTarget = pressedKey.popup.firstOrNull()
                    if (popupTarget != null && popupTarget.isNotEmpty()) {
                        longPressRunnable = Runnable {
                            longPressTriggered = true
                            isGliding = false
                            glideSamples.clear()
                            onKeyListener?.invoke(popupTarget.first().code, popupTarget)
                            try {
                                performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                            } catch (_: Exception) {}
                        }
                        longPressHandler.postDelayed(longPressRunnable!!, 380L)
                    }
                }
                
                // Add ripple on tap (cap to maxRipples to prevent memory bloat)
                if (ripples.size < maxRipples) {
                    ripples.add(Ripple(event.x, event.y, glideStartTime))
                    postInvalidateOnAnimation()
                }
                
                val sample = sampleAt(event.x, event.y)
                if (sample.char != ' ') glideSamples.add(sample)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = kotlin.math.abs(event.x - touchDownX)
                val dy = kotlin.math.abs(event.y - touchDownY)
                if (dx > 14f * density || dy > 14f * density) {
                    longPressRunnable?.let { longPressHandler.removeCallbacks(it) }
                }

                val elapsed = System.currentTimeMillis() - glideStartTime
                if (!isGliding && elapsed > 80L && glideEnabled) {
                    isGliding = true
                }
                if (isGliding) {
                    val sample = sampleAt(event.x, event.y)
                    if (sample.char != ' ' && (glideSamples.isEmpty() || sample.char != glideSamples.last().char)) {
                        glideSamples.add(sample)
                    }
                    // Append trail point for gradient fade
                    glideTrail.add(TrailPoint(event.x, event.y, System.currentTimeMillis()))
                    postInvalidateOnAnimation()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                longPressRunnable?.let { longPressHandler.removeCallbacks(it) }
                if (longPressTriggered) {
                    isGliding = false
                    glideSamples.clear()
                    postInvalidateOnAnimation()
                    return true
                }

                if (isResizeMode) {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    if (resizePlusBounds.contains(x, y) || (x >= resizePlusBounds.left - 10 && x <= resizePlusBounds.right + 10 && y >= resizePlusBounds.top - 10 && y <= resizePlusBounds.bottom + 10)) {
                        val newHeight = (keyHeightDp + 4).coerceAtMost(96)
                        keyHeightDp = newHeight
                        onHeightChanged?.invoke(newHeight)
                        requestLayout()
                        invalidate()
                        return true
                    } else if (resizeMinusBounds.contains(x, y) || (x >= resizeMinusBounds.left - 10 && x <= resizeMinusBounds.right + 10 && y >= resizeMinusBounds.top - 10 && y <= resizeMinusBounds.bottom + 10)) {
                        val newHeight = (keyHeightDp - 4).coerceAtLeast(48)
                        keyHeightDp = newHeight
                        onHeightChanged?.invoke(newHeight)
                        requestLayout()
                        invalidate()
                        return true
                    } else if (resizeDoneBounds.contains(x, y) || (x >= resizeDoneBounds.left - 10 && x <= resizeDoneBounds.right + 10 && y >= resizeDoneBounds.top - 10 && y <= resizeDoneBounds.bottom + 10)) {
                        isResizeMode = false
                        onHeightChanged?.invoke(keyHeightDp)
                        requestLayout()
                        invalidate()
                        return true
                    }
                }

                if (isGliding && glideSamples.size >= 2) {
                    val decoded = glideDecoder.decode(glideSamples)
                    if (decoded.isNotEmpty()) {
                        onGlideListener?.invoke(decoded.first())
                    }
                } else if (!isGliding && glideSamples.isNotEmpty()) {
                    // Tap on a single key
                    val key = findKeyAt(event.x, event.y)
                    if (key != null) {
                        onKeyListener?.invoke(key.code, key.label)
                    }
                } else if (!isGliding && predictionEnabled) {
                    // Check if suggestion was tapped
                    var handled = false
                    for ((sugg, rect) in suggestionBounds) {
                        if (rect.contains(event.x.toInt(), event.y.toInt())) {
                            onGlideListener?.invoke(sugg)
                            handled = true
                            break
                        }
                    }
                    if (!handled) {
                        for ((action, rect) in toolbarBounds) {
                            if (rect.contains(event.x.toInt(), event.y.toInt())) {
                                when (action) {
                                    "hub" -> onOpenHub?.invoke() ?: onKeyListener?.invoke(KC.SETTINGS, "Settings")
                                    "emoji" -> onKeyListener?.invoke(KC.EMOJI, "Emoji")
                                    "language" -> onKeyListener?.invoke(KC.LANGUAGE, "Language")
                                    "clipboard" -> onOpenClipboard?.invoke() ?: onKeyListener?.invoke(KC.CLIPBOARD, "Clipboard")
                                    "audio_wave", "mic" -> onOpenHub?.invoke() ?: onKeyListener?.invoke(KC.SETTINGS, "Settings")
                                    "cursor" -> onOpenTextEditing?.invoke() ?: onKeyListener?.invoke(KC.SETTINGS, "Settings")
                                    "hide" -> onHideKeyboard?.invoke()
                                }
                                break
                            }
                        }
                    }
                }
                isGliding = false
                glideSamples.clear()
                // Trail will fade naturally via alpha in onDraw
                postInvalidateOnAnimation()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findKeyAt(x: Float, y: Float): Key? {
        for ((key, rect) in keyBounds) {
            if (rect.contains(x.toInt(), y.toInt())) {
                return key
            }
        }
        return null
    }

    private fun sampleAt(x: Float, y: Float): GlideDecoder.Sample {
        for ((key, rect) in keyBounds) {
            if (rect.contains(x.toInt(), y.toInt())) {
                // Use the actual label character, respecting shift state for letters
                var char = key.label[0]
                if (shiftActive && char.isLetter()) {
                    char = char.uppercaseChar()
                }
                return GlideDecoder.Sample(char, x, y)
            }
        }
        return GlideDecoder.Sample(' ', x, y)
    }

    fun setOnKeyListener(listener: (Int, String) -> Unit) {
        onKeyListener = listener
    }

    fun setOnGlideListener(listener: (String) -> Unit) {
        onGlideListener = listener
    }

    fun setSuggestions(sugg: List<String>) {
        suggestions = emptyList()
        invalidate()
    }

    fun setPredictionEnabled(enabled: Boolean) {
        if (predictionEnabled == enabled) return
        predictionEnabled = enabled
        if (!enabled) {
            suggestions = emptyList()
            suggestionBounds.clear()
        }
        requestLayout()
        invalidate()
    }

    fun setInputProfile(predictionsEnabled: Boolean, glideEnabled: Boolean, keyHeightDp: Int) {
        setPredictionEnabled(predictionsEnabled)
        if (this.glideEnabled != glideEnabled) {
            this.glideEnabled = glideEnabled
            if (!glideEnabled) {
                isGliding = false
                glideSamples.clear()
                glideTrail.clear()
            }
        }
        if (this.keyHeightDp != keyHeightDp) {
            this.keyHeightDp = keyHeightDp
            requestLayout()
        }
        if (!predictionsEnabled && !glideEnabled) {
            isGliding = false
            glideSamples.clear()
            glideTrail.clear()
        }
        invalidate()
    }

    fun setKeyHeightDp(heightDp: Int) {
        if (this.keyHeightDp != heightDp) {
            this.keyHeightDp = heightDp
            requestLayout()
            invalidate()
        }
    }

    fun setShift(active: Boolean) {
        shiftActive = active
        invalidate()
    }

    private fun drawNumpad(
        canvas: Canvas,
        w: Float,
        yStart: Float,
        density: Float,
        keyHeightPx: Float,
        rowSpacingPx: Float
    ) {
        val keyPadding = 2.5f * density
        val keyCornerRadius = 6f * density

        val col1Weight = 1.15f
        val col2Weight = 2.0f
        val col3Weight = 2.0f
        val col4Weight = 2.0f
        val col5Weight = 1.15f
        val totalWeight = 8.3f

        val col1Width = w * (col1Weight / totalWeight)
        val col2Width = w * (col2Weight / totalWeight)
        val col3Width = w * (col3Weight / totalWeight)
        val col4Width = w * (col4Weight / totalWeight)
        val col5Width = w * (col5Weight / totalWeight)

        val xCol1 = 0f
        val xCol2 = col1Width
        val xCol3 = xCol2 + col2Width
        val xCol4 = xCol3 + col3Width
        val xCol5 = xCol4 + col4Width

        val rows123Height = keyHeightPx * 3f + rowSpacingPx * 2f

        fun drawKey(
            key: Key,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            label: String = key.label,
            customBgColor: Int? = null,
            customTextColor: Int? = null,
            customRadius: Float = keyCornerRadius,
            fontSizeRatio: Float = 0.44f
        ) {
            val rect = Rect(
                (left + keyPadding).toInt(),
                (top + keyPadding).toInt(),
                (right - keyPadding).toInt(),
                (bottom - keyPadding).toInt()
            )
            keyBounds[key] = rect

            val bg = customBgColor ?: if (key.isModifier) theme.keyModifierBackground else theme.keyBackground
            keyBgPaint.color = bg
            canvas.drawRoundRect(
                left + keyPadding, top + keyPadding, right - keyPadding, bottom - keyPadding,
                customRadius, customRadius,
                keyBgPaint
            )
            canvas.drawRoundRect(
                left + keyPadding, top + keyPadding, right - keyPadding, bottom - keyPadding,
                customRadius, customRadius,
                keyBorderPaint
            )

            val centerX = (left + right) / 2f
            val centerY = (top + bottom) / 2f

            when (key.code) {
                KC.DELETE -> {
                    deleteIcon?.let { icon ->
                        val iconSize = ((bottom - top) * 0.44f).toInt()
                        val iconLeft = (centerX - iconSize / 2f).toInt()
                        val iconTop = (centerY - iconSize / 2f).toInt()
                        icon.setTint(theme.keyText)
                        icon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                        icon.draw(canvas)
                    }
                }
                KC.ENTER -> {
                    val icon = numpadEnterIcon ?: enterIcon
                    icon?.let {
                        val iconSize = ((bottom - top) * 0.46f).toInt()
                        val iconLeft = (centerX - iconSize / 2f).toInt()
                        val iconTop = (centerY - iconSize / 2f).toInt()
                        it.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                        it.draw(canvas)
                    }
                }
                KC.SPACE -> {
                    val bracketW = (right - left) * 0.40f
                    val bracketH = (bottom - top) * 0.16f
                    val bLeft = centerX - bracketW / 2f
                    val bRight = centerX + bracketW / 2f
                    val bBottom = centerY + bracketH / 2f
                    val bTop = centerY - bracketH / 2f

                    val spacePath = android.graphics.Path().apply {
                        moveTo(bLeft, bTop)
                        lineTo(bLeft, bBottom)
                        lineTo(bRight, bBottom)
                        lineTo(bRight, bTop)
                    }
                    val spacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = customTextColor ?: theme.keyText
                        style = Paint.Style.STROKE
                        strokeWidth = 2.4f * density
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    canvas.drawPath(spacePath, spacePaint)
                }
                else -> {
                    textPaint.color = customTextColor ?: theme.keyText
                    textPaint.textSize = (bottom - top) * fontSizeRatio
                    val baseline = centerY - ((textPaint.ascent() + textPaint.descent()) / 2f)
                    canvas.drawText(label, centerX, baseline, textPaint)
                }
            }
        }

        // 1. Column 1: 4 operator keys (+, -, *, /) spanning rows123Height
        val opKeys = listOf(
            Key("+", '+'.code),
            Key("-", '-'.code),
            Key("*", '*'.code),
            Key("/", '/'.code),
        )
        val opHeight = (rows123Height - 3f * rowSpacingPx) / 4f
        for (i in 0 until 4) {
            val top = yStart + i * (opHeight + rowSpacingPx)
            val bottom = top + opHeight
            drawKey(opKeys[i], xCol1, top, xCol2, bottom, fontSizeRatio = 0.42f)
        }

        // 2. Rows 1, 2, 3: Numbers and side keys
        val row1Y = yStart
        val row2Y = yStart + keyHeightPx + rowSpacingPx
        val row3Y = yStart + 2f * (keyHeightPx + rowSpacingPx)

        // Row 1: 1, 2, 3, %
        drawKey(Key("1", '1'.code), xCol2, row1Y, xCol3, row1Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("2", '2'.code), xCol3, row1Y, xCol4, row1Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("3", '3'.code), xCol4, row1Y, xCol5, row1Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("%", '%'.code), xCol5, row1Y, w, row1Y + keyHeightPx, fontSizeRatio = 0.42f)

        // Row 2: 4, 5, 6, ␣
        drawKey(Key("4", '4'.code), xCol2, row2Y, xCol3, row2Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("5", '5'.code), xCol3, row2Y, xCol4, row2Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("6", '6'.code), xCol4, row2Y, xCol5, row2Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("␣", KC.SPACE), xCol5, row2Y, w, row2Y + keyHeightPx)

        // Row 3: 7, 8, 9, Delete
        drawKey(Key("7", '7'.code), xCol2, row3Y, xCol3, row3Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("8", '8'.code), xCol3, row3Y, xCol4, row3Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("9", '9'.code), xCol4, row3Y, xCol5, row3Y + keyHeightPx, fontSizeRatio = 0.52f)
        drawKey(Key("Delete", KC.DELETE, isModifier = true), xCol5, row3Y, w, row3Y + keyHeightPx)

        // 3. Row 4 (Bottom Row): ABC, comma, !?#, 0, =, ., Enter
        val row4Y = yStart + rows123Height + rowSpacingPx
        val row4Bottom = row4Y + keyHeightPx

        drawKey(
            Key("ABC", KC.ABC, isModifier = true),
            xCol1, row4Y, xCol2, row4Bottom,
            fontSizeRatio = 0.34f,
            customRadius = keyHeightPx * 0.45f
        )

        val commaWidth = col2Width * (0.9f / 2.0f)
        drawKey(Key(",", ','.code), xCol2, row4Y, xCol2 + commaWidth, row4Bottom, fontSizeRatio = 0.48f)
        drawKey(Key("!?#", KC.SHIFT_SYMBOLS, isModifier = true), xCol2 + commaWidth, row4Y, xCol3, row4Bottom, fontSizeRatio = 0.30f)

        drawKey(Key("0", '0'.code), xCol3, row4Y, xCol4, row4Bottom, fontSizeRatio = 0.52f)

        val equalsWidth = col4Width * (1.1f / 2.0f)
        drawKey(Key("=", '='.code), xCol4, row4Y, xCol4 + equalsWidth, row4Bottom, fontSizeRatio = 0.44f)
        drawKey(Key(".", '.'.code), xCol4 + equalsWidth, row4Y, xCol5, row4Bottom, fontSizeRatio = 0.48f)

        drawKey(
            Key("Enter", KC.ENTER, isModifier = true),
            xCol5, row4Y, w, row4Bottom,
            customBgColor = 0xFFA8C7FA.toInt(),
            customTextColor = 0xFF041E49.toInt(),
            customRadius = keyHeightPx * 0.45f
        )
    }

    fun updateLayout(layout: KeyLayout) {
        keyLayout = layout
        // Recompute effective layout with number row if enabled (skip for numpad and symbols)
        effectiveLayout = if (layout.id == "numpad" || layout.id.startsWith("symbols")) {
            layout
        } else if (settings.numberRow) {
            val numberRow = (1..10).map { i ->
                val ch = if (i == 10) '0' else (i + 48).toChar()
                Key(ch.toString(), ch.code)
            }
            KeyLayout(
                layout.id + "_with_numbers",
                listOf(numberRow) + layout.rows
            )
        } else {
            layout
        }
        requestLayout()
        invalidate()
    }

    fun updateDictionary(wordList: WordList, userDict: UserDictionary) {
        this.wordList = wordList
        this.userDict = userDict
        glideDecoder = GlideDecoder(wordList, userDict)
    }
}
