package com.openswift.keyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.openswift.keyboard.R

/**
 * Quick Actions / Tools Hub View matching the user's reference image (الصورة رقم 91):
 * - Top toolbar with 5 icons, where the Monogram hub icon is actively highlighted.
 * - 2 columns of rounded capsule pill buttons:
 *   Right Column: [تغيير الحجم], [< المظهر], [الصوت], [ترجمة], [اللغة التالية]
 *   Left Column:  [< الإعدادات], [< ملف GIF], [< ملصق], [عائم], [< رموز الإيموجي]
 * - Bottom row: Edit button (pencil icon) on the left and pagination dots in the center.
 */
class ToolsHubView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    enum class ToolId {
        RESIZE,
        THEME,
        VOICE,
        TRANSLATE,
        NEXT_LANGUAGE,
        SETTINGS,
        GIF,
        STICKER,
        FLOATING,
        EMOJI,
        CUSTOMIZE
    }

    var onOpenEmoji: (() -> Unit)? = null
    var onOpenTextEditing: (() -> Unit)? = null
    var onOpenClipboard: (() -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onToolSelected: ((ToolId) -> Unit)? = null

    var keyHeightDp = 78
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private val density = resources.displayMetrics.density

    // Colors matching screenshot #91
    private val colorBg = 0xFF14171E.toInt()
    private val colorPillBg = 0xFF282D37.toInt()
    private val colorToolbarIcon = 0xFFD8DCE5.toInt()
    private val colorHighlightBg = 0xFF1E3F42.toInt()
    private val colorTextWhite = 0xFFFFFFFF.toInt()
    private val colorChevron = 0xFF9EACB8.toInt()

    // Paints
    private val bgPaint = Paint().apply {
        color = colorBg
        style = Paint.Style.FILL
    }
    private val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPillBg
        style = Paint.Style.FILL
    }
    private val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        textSize = 14.5f * density
        color = colorTextWhite
        isFakeBoldText = true
    }
    private val chevronPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = 16f * density
        color = colorChevron
    }
    private val highlightBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorHighlightBg
        style = Paint.Style.FILL
    }
    private val bottomDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val editPillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x44FFFFFF
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }

    // Top toolbar Drawables
    private val hubIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_hub_monogram)?.mutate()?.apply {
        setTint(0xFFFFFFFF.toInt())
    }
    private val emojiIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_sentiment_satisfied)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }
    private val cursorIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_text_cursor)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }
    private val clipboardIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_content_paste)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }
    private val hideIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_keyboard_arrow_down)?.mutate()?.apply {
        setTint(colorToolbarIcon)
    }

    // Tool Icons
    private val resizeIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_resize)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val themeIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_palette)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val micIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_mic)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val translateIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_translate)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val globeIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_language)?.mutate()?.apply { setTint(colorToolbarIcon) }

    private val settingsIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_settings)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val gifIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_gif)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val stickerIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_sticker)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val floatingIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_floating)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val smileyFaceIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_sentiment_satisfied)?.mutate()?.apply { setTint(colorToolbarIcon) }
    private val pencilIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_edit_pencil)?.mutate()?.apply { setTint(colorToolbarIcon) }

    // Hit test bounds
    private val toolbarBounds = mutableMapOf<String, RectF>()
    private val toolBounds = mutableMapOf<ToolId, RectF>()

    // Dimensions
    private val toolbarHeight = 44f * density
    private val horizontalMargin = 12f * density
    private val colGap = 10f * density
    private val rowGap = 7f * density
    private val pillHeight = 36f * density
    private val bottomBarHeight = 32f * density

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

        // 3. Pill Grid
        drawPillGrid(canvas, w)

        // 4. Bottom Row (pencil pill + pagination dots)
        drawBottomBar(canvas, w, h)
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

            // Monogram icon (index 0) is actively highlighted
            if (id == "hub") {
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

    private data class PillItem(
        val id: ToolId,
        val label: String,
        val icon: Drawable?,
        val hasChevron: Boolean
    )

    private fun drawPillGrid(canvas: Canvas, w: Float) {
        toolBounds.clear()
        val gridTop = toolbarHeight + (6f * density)
        val totalAvailableWidth = w - (horizontalMargin * 2f) - colGap
        val colWidth = totalAvailableWidth / 2f

        // Right column items (RTL: right side)
        val rightItems = listOf(
            PillItem(ToolId.RESIZE, "تغيير الحجم", resizeIcon, hasChevron = false),
            PillItem(ToolId.THEME, "المظهر", themeIcon, hasChevron = true),
            PillItem(ToolId.VOICE, "الصوت", micIcon, hasChevron = false),
            PillItem(ToolId.TRANSLATE, "ترجمة", translateIcon, hasChevron = false),
            PillItem(ToolId.NEXT_LANGUAGE, "اللغة التالية", globeIcon, hasChevron = false)
        )

        // Left column items (RTL: left side)
        val leftItems = listOf(
            PillItem(ToolId.SETTINGS, "الإعدادات", settingsIcon, hasChevron = true),
            PillItem(ToolId.GIF, "ملف GIF", gifIcon, hasChevron = true),
            PillItem(ToolId.STICKER, "ملصق", stickerIcon, hasChevron = true),
            PillItem(ToolId.FLOATING, "عائم", floatingIcon, hasChevron = false),
            PillItem(ToolId.EMOJI, "رموز الإيموجي", smileyFaceIcon, hasChevron = true)
        )

        val numRows = 5
        for (row in 0 until numRows) {
            val y = gridTop + row * (pillHeight + rowGap)

            // Draw Right Pill
            val rightItem = rightItems[row]
            val rightX = w - horizontalMargin - colWidth
            val rightRect = RectF(rightX, y, rightX + colWidth, y + pillHeight)
            toolBounds[rightItem.id] = rightRect
            drawPill(canvas, rightRect, rightItem)

            // Draw Left Pill
            val leftItem = leftItems[row]
            val leftX = horizontalMargin
            val leftRect = RectF(leftX, y, leftX + colWidth, y + pillHeight)
            toolBounds[leftItem.id] = leftRect
            drawPill(canvas, leftRect, leftItem)
        }
    }

    private fun drawPill(canvas: Canvas, rect: RectF, item: PillItem) {
        val radius = rect.height() / 2f
        canvas.drawRoundRect(rect, radius, radius, pillBgPaint)

        val centerY = rect.centerY()
        val iconSize = (19f * density).toInt()

        // 1. Icon on the right side of the pill
        val iconRight = rect.right - (12f * density)
        val iconLeft = iconRight - iconSize
        val iconTop = (centerY - iconSize / 2f).toInt()
        val iconBottom = iconTop + iconSize
        item.icon?.setBounds(iconLeft.toInt(), iconTop, iconRight.toInt(), iconBottom)
        item.icon?.draw(canvas)

        // 2. Text in the middle/aligned right
        val textRight = iconLeft - (8f * density)
        val textY = centerY - ((pillTextPaint.ascent() + pillTextPaint.descent()) / 2f)
        canvas.drawText(item.label, textRight, textY, pillTextPaint)

        // 3. Optional left chevron `<` on the far left
        if (item.hasChevron) {
            val chevronX = rect.left + (12f * density)
            val chevronY = centerY - ((chevronPaint.ascent() + chevronPaint.descent()) / 2f)
            canvas.drawText("‹", chevronX, chevronY, chevronPaint)
        }
    }

    private fun drawBottomBar(canvas: Canvas, w: Float, h: Float) {
        val bottomY = h - bottomBarHeight - (6f * density)

        // Edit button (pill with pencil) on the left
        val editWidth = 52f * density
        val editHeight = 24f * density
        val editRect = RectF(horizontalMargin, bottomY, horizontalMargin + editWidth, bottomY + editHeight)
        toolBounds[ToolId.CUSTOMIZE] = editRect

        canvas.drawRoundRect(editRect, editHeight / 2f, editHeight / 2f, pillBgPaint)
        canvas.drawRoundRect(editRect, editHeight / 2f, editHeight / 2f, editPillBorderPaint)

        val pencilSize = (14f * density).toInt()
        val pLeft = (editRect.centerX() - pencilSize / 2f).toInt()
        val pTop = (editRect.centerY() - pencilSize / 2f).toInt()
        pencilIcon?.setBounds(pLeft, pTop, pLeft + pencilSize, pTop + pencilSize)
        pencilIcon?.draw(canvas)

        // Pagination Dots in the center: dot 1 (gray), dot 2 (white)
        val dotRadius = 3f * density
        val dotGap = 6f * density
        val dotsCenterX = w / 2f
        val dotsCenterY = editRect.centerY()

        // Dot 1 (inactive gray)
        bottomDotPaint.color = 0xFF58606E.toInt()
        canvas.drawCircle(dotsCenterX - dotRadius - (dotGap / 2f), dotsCenterY, dotRadius, bottomDotPaint)

        // Dot 2 (active white)
        bottomDotPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(dotsCenterX + dotRadius + (dotGap / 2f), dotsCenterY, dotRadius, bottomDotPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Check Toolbar clicks
            for ((id, rect) in toolbarBounds) {
                if (rect.contains(event.x, event.y)) {
                    when (id) {
                        "emoji" -> onOpenEmoji?.invoke()
                        "cursor" -> onOpenTextEditing?.invoke()
                        "clipboard" -> onOpenClipboard?.invoke()
                        "hide" -> onClose?.invoke()
                    }
                    return true
                }
            }

            // Check Tool clicks
            for ((id, rect) in toolBounds) {
                if (rect.contains(event.x, event.y)) {
                    when (id) {
                        ToolId.EMOJI -> onOpenEmoji?.invoke()
                        else -> onToolSelected?.invoke(id)
                    }
                    return true
                }
            }
        }
        return true
    }
}
