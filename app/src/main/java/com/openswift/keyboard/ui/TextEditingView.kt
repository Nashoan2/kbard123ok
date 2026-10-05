package com.openswift.keyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.openswift.keyboard.R

/**
 * Text Editing and Cursor Navigation View matching the user's reference image (الصورة رقم 90):
 * - Top toolbar with 5 icons, where the <|> cursor icon is actively highlighted with a teal rounded box.
 * - 3 rows x 5 columns grid of keypad buttons:
 *   Row 1: [ حافظة النصوص ]  [  ^  ]  [ الكل ]  [ نسخ ]  [ ⌫ (Delete) ]
 *   Row 2: [      <       ]  [تحديد]  [  >  ]  [ قص  ]  [ — (Space)  ]
 *   Row 3: [الصفحة الرئيسية]  [  v  ]  [إنهاء]  [ لصق ]  [ ↶ (Undo)   ]
 */
class TextEditingView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    enum class Action {
        CLIPBOARD,
        UP,
        SELECT_ALL,
        COPY,
        DELETE,
        LEFT,
        SELECT_TOGGLE,
        RIGHT,
        CUT,
        SPACE,
        HOME,
        DOWN,
        END,
        PASTE,
        UNDO
    }

    var onOpenHub: (() -> Unit)? = null
    var onOpenEmoji: (() -> Unit)? = null
    var onOpenClipboard: (() -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onAction: ((Action) -> Unit)? = null

    var isSelectionModeActive = false
        set(value) {
            field = value
            invalidate()
        }

    var keyHeightDp = 78
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private val density = resources.displayMetrics.density

    // Colors matching screenshot #90
    private val colorBg = 0xFF14171E.toInt() // Dark keyboard background
    private val colorKeyBg = 0xFF242831.toInt() // Dark slate key background
    private val colorKeyActiveBg = 0xFF1D3B3E.toInt() // Key active/selected
    private val colorTeal = 0xFF2DD4BF.toInt() // Vibrant cyan/teal
    private val colorTextWhite = 0xFFFFFFFF.toInt()
    private val colorToolbarIcon = 0xFFD8DCE5.toInt()

    // Paints
    private val bgPaint = Paint().apply {
        color = colorBg
        style = Paint.Style.FILL
    }
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorKeyBg
        style = Paint.Style.FILL
    }
    private val keyActiveBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorKeyActiveBg
        style = Paint.Style.FILL
    }
    private val keyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 16f * density
        color = colorTextWhite
        isFakeBoldText = true
    }
    private val smallMultiLineTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 13.5f * density
        color = colorTextWhite
        isFakeBoldText = true
    }
    private val arrowGlyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 22f * density
        color = colorTeal
        isFakeBoldText = true
    }
    private val highlightBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1E3F42.toInt() // Dark teal highlight for active toolbar icon
        style = Paint.Style.FILL
    }

    // Top toolbar Drawables
    private val hubIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_hub_monogram)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }
    private val emojiIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_sentiment_satisfied)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }
    private val cursorIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_text_cursor)?.mutate()?.apply {
        setTint(0xFFFFFFFF.toInt())
    }
    private val clipboardIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_content_paste)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }
    private val hideIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_keyboard_arrow_down)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }

    // Keypad Action Drawables (Cyan/Teal)
    private val deleteIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_edit_backspace)?.mutate()
    private val spaceIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_edit_space)?.mutate()
    private val undoIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_undo)?.mutate()

    // Hit test regions
    private val toolbarBounds = mutableMapOf<String, RectF>()
    private val keyBounds = mutableMapOf<Action, RectF>()

    // Dimensions
    private val toolbarHeight = 44f * density
    private val keyCornerRadius = 6f * density
    private val keyPadding = 4f * density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val keyHeightPx = keyHeightDp * density
        val targetHeight = (4 * keyHeightPx) + (2f * density * 3) + (keyHeightPx * 0.82f) + (8f * density)
        setMeasuredDimension(w, targetHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Background
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // 2. Toolbar
        drawToolbar(canvas, w)

        // 3. 3x5 Keypad Grid
        drawKeypad(canvas, w, h)
    }

    private fun drawToolbar(canvas: Canvas, w: Float) {
        toolbarBounds.clear()
        val numIcons = 5
        val slotWidth = w / numIcons.toFloat()
        val iconSize = (22f * density).toInt()

        val icons = listOf(
            "hub" to hubIcon,
            "emoji" to emojiIcon,
            "cursor" to cursorIcon,
            "clipboard" to clipboardIcon,
            "hide" to hideIcon
        )

        icons.forEachIndexed { i, (id, drawable) ->
            val centerX = (i * slotWidth) + (slotWidth / 2f)
            val centerY = toolbarHeight / 2f

            val slotRect = RectF(i * slotWidth, 0f, (i + 1) * slotWidth, toolbarHeight)
            toolbarBounds[id] = slotRect

            // Cursor icon (index 2) is highlighted with rounded teal rectangle
            if (id == "cursor") {
                val boxWidth = slotWidth * 0.72f
                val boxHeight = toolbarHeight * 0.74f
                val highlightRect = RectF(
                    centerX - boxWidth / 2f,
                    centerY - boxHeight / 2f,
                    centerX + boxWidth / 2f,
                    centerY + boxHeight / 2f
                )
                canvas.drawRoundRect(highlightRect, 8f * density, 8f * density, highlightBoxPaint)
            }

            if (drawable != null) {
                val left = (centerX - iconSize / 2f).toInt()
                val top = (centerY - iconSize / 2f).toInt()
                drawable.setBounds(left, top, left + iconSize, top + iconSize)
                drawable.draw(canvas)
            }
        }
    }

    private fun drawKeypad(canvas: Canvas, w: Float, h: Float) {
        keyBounds.clear()
        val keypadTop = toolbarHeight + (4f * density)
        val keypadBottom = h - (6f * density)
        val keypadHeight = keypadBottom - keypadTop

        val rows = 3
        val cols = 5
        val rowHeight = (keypadHeight - (keyPadding * (rows - 1))) / rows.toFloat()
        val colWidth = (w - (keyPadding * (cols + 1))) / cols.toFloat()

        // 3x5 layout definition
        val grid = listOf(
            listOf(
                KeyItem(Action.CLIPBOARD, "حافظة\nالنصوص", isMultiLine = true),
                KeyItem(Action.UP, "^", isArrow = true),
                KeyItem(Action.SELECT_ALL, "الكل"),
                KeyItem(Action.COPY, "نسخ"),
                KeyItem(Action.DELETE, "", icon = deleteIcon)
            ),
            listOf(
                KeyItem(Action.LEFT, "<", isArrow = true),
                KeyItem(Action.SELECT_TOGGLE, "تحديد"),
                KeyItem(Action.RIGHT, ">", isArrow = true),
                KeyItem(Action.CUT, "قص"),
                KeyItem(Action.SPACE, "", icon = spaceIcon)
            ),
            listOf(
                KeyItem(Action.HOME, "الصفحة\nالرئيسية", isMultiLine = true),
                KeyItem(Action.DOWN, "v", isArrow = true),
                KeyItem(Action.END, "إنهاء"),
                KeyItem(Action.PASTE, "لصق"),
                KeyItem(Action.UNDO, "", icon = undoIcon)
            )
        )

        grid.forEachIndexed { r, row ->
            val top = keypadTop + r * (rowHeight + keyPadding)
            val bottom = top + rowHeight

            row.forEachIndexed { c, item ->
                val left = keyPadding + c * (colWidth + keyPadding)
                val right = left + colWidth
                val rect = RectF(left, top, right, bottom)
                keyBounds[item.action] = rect

                // Background
                val isSelectionActive = (item.action == Action.SELECT_TOGGLE && isSelectionModeActive)
                val paintToUse = if (isSelectionActive) keyActiveBgPaint else keyBgPaint
                canvas.drawRoundRect(rect, keyCornerRadius, keyCornerRadius, paintToUse)

                // Content
                val centerX = rect.centerX()
                val centerY = rect.centerY()

                when {
                    item.icon != null -> {
                        val iconSize = (22f * density).toInt()
                        val iLeft = (centerX - iconSize / 2f).toInt()
                        val iTop = (centerY - iconSize / 2f).toInt()
                        item.icon.setBounds(iLeft, iTop, iLeft + iconSize, iTop + iconSize)
                        item.icon.draw(canvas)
                    }
                    item.isArrow -> {
                        val glyph = when (item.action) {
                            Action.UP -> "︿"
                            Action.DOWN -> "﹀"
                            Action.LEFT -> "〈"
                            Action.RIGHT -> "〉"
                            else -> item.label
                        }
                        val textY = centerY - ((arrowGlyphPaint.ascent() + arrowGlyphPaint.descent()) / 2f)
                        canvas.drawText(glyph, centerX, textY, arrowGlyphPaint)
                    }
                    item.isMultiLine -> {
                        val lines = item.label.split("\n")
                        val lineHeight = 16f * density
                        val totalHeight = (lines.size - 1) * lineHeight
                        val startY = centerY - (totalHeight / 2f) - ((smallMultiLineTextPaint.ascent() + smallMultiLineTextPaint.descent()) / 2f)
                        lines.forEachIndexed { idx, line ->
                            canvas.drawText(line, centerX, startY + (idx * lineHeight), smallMultiLineTextPaint)
                        }
                    }
                    else -> {
                        val textY = centerY - ((keyTextPaint.ascent() + keyTextPaint.descent()) / 2f)
                        canvas.drawText(item.label, centerX, textY, keyTextPaint)
                    }
                }
            }
        }
    }

    private data class KeyItem(
        val action: Action,
        val label: String,
        val isArrow: Boolean = false,
        val isMultiLine: Boolean = false,
        val icon: android.graphics.drawable.Drawable? = null
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Check Toolbar clicks
            for ((id, rect) in toolbarBounds) {
                if (rect.contains(event.x, event.y)) {
                    when (id) {
                        "hub" -> onOpenHub?.invoke()
                        "emoji" -> onOpenEmoji?.invoke()
                        "clipboard" -> onOpenClipboard?.invoke()
                        "hide" -> onClose?.invoke()
                    }
                    return true
                }
            }

            // Check Keypad clicks
            for ((action, rect) in keyBounds) {
                if (rect.contains(event.x, event.y)) {
                    if (action == Action.CLIPBOARD) {
                        onOpenClipboard?.invoke()
                    } else {
                        onAction?.invoke(action)
                    }
                    return true
                }
            }
        }
        return true
    }
}
