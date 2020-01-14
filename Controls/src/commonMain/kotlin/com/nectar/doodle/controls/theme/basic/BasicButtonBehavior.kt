package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.theme.AbstractTextButtonBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import kotlin.math.max


/**
 * Created by Nicholas Eddy on 3/17/18.
 */
class BasicButtonBehavior(
        private val textMetrics        : TextMetrics,
        private val backgroundColor    : Color,
        private val darkBackgroundColor: Color,
        private val foregroundColor    : Color,
        private val borderColor        : Color,
        private val borderWidth        : Double = 1.0): AbstractTextButtonBehavior<Button>(textMetrics) {

    private var insets = Insets(4.0)

    override fun render(view: Button, canvas: Canvas) {
        val model       = view.model
        var fillColor   = if (model.selected || model.pressed && model.armed) darkBackgroundColor else view.backgroundColor ?: backgroundColor
        var textColor   = view.foregroundColor ?: foregroundColor
        var borderColor = borderColor

        if (!view.enabled) {
            textColor   = textColor.lighter  ()
            fillColor   = fillColor.lighter  ()
            borderColor = borderColor.lighter()
        }

        val penWidth = if (view.enabled && (model.pressed || model.mouseOver)) 2 * borderWidth else borderWidth

        canvas.rect(Rectangle(size = view.size).inset(penWidth / 2), Pen(borderColor, penWidth), ColorBrush(fillColor))

        val icon = icon(view)
        val text = view.text

        if (text.isNotBlank()) {
            canvas.text(text, font(view), textPosition(view, icon = icon), ColorBrush(textColor))
        }

        icon?.render(view, canvas, iconPosition(view, icon = icon))
    }

    override fun install(view: Button) {
        super.install(view)

        recalculateSize(view)
    }

    private fun recalculateSize(button: Button) {
        val icon   = if (button.enabled) button.icon else button.disabledIcon
        var size   = textMetrics.size(button.text, font(button))
        var width  = size.width
        var height = size.height

        if (icon != null) {
            val iconWidth = icon.size.width

            if (iconWidth > 0) {
                width += button.iconTextSpacing
            }

            width  += iconWidth
            height  = max(height, icon.size.height)
        }

        size = Size(width + insets.left + insets.right, height + insets.top + insets.bottom)

        button.idealSize = size

//        button.setSize(size)
    }
}
