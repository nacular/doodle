package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.theme.CommonTextButtonBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Lightgray
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.darker
import com.nectar.doodle.drawing.lighter
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import kotlin.math.max


/**
 * Created by Nicholas Eddy on 3/17/18.
 */
open class BasicButtonBehavior(
        private val textMetrics        : TextMetrics,
        private val backgroundColor    : Color  = Lightgray,
        private val darkBackgroundColor: Color  = backgroundColor.darker(),
        private val foregroundColor    : Color  = White,
        private val borderColor        : Color? = null,
        private val borderWidth        : Double = 0.0,
        private val cornerRadius       : Double = 4.0,
                    insets             : Double = 4.0): CommonTextButtonBehavior<Button>(textMetrics) {

    private var insets = Insets(insets)

    protected data class RenderColors(val fillColor: Color, val textColor: Color, val borderColor: Color?)

    protected fun colors(view: Button): RenderColors {
        val model       = view.model
        var fillColor   = if (model.selected || model.pressed && model.armed) darkBackgroundColor else view.backgroundColor ?: backgroundColor
        var textColor   = view.foregroundColor ?: foregroundColor
        var borderColor = borderColor

        if (!view.enabled) {
            textColor   = textColor.lighter   ()
            fillColor   = fillColor.lighter   ()
            borderColor = borderColor?.lighter()
        } else if (model.pointerOver) {
            fillColor = fillColor.darker(0.1f)
        }

        return RenderColors(fillColor, textColor, borderColor)
    }

    override fun render(view: Button, canvas: Canvas) {
        val model  = view.model
        val colors = colors(view)

        val penWidth = if (view.enabled && (model.pressed || model.pointerOver)) 2 * borderWidth else borderWidth

        if (penWidth > 0 && colors.borderColor != null) {
            canvas.rect(Rectangle(size = view.size).inset(penWidth / 2), cornerRadius, Pen(colors.borderColor, penWidth), ColorBrush(colors.fillColor))
        } else {
            canvas.rect(Rectangle(size = view.size), cornerRadius, ColorBrush(colors.fillColor))
        }

        val icon = icon(view)
        val text = view.text
        var textPosition: Point? = null

        if (text.isNotBlank()) {
            textPosition = textPosition(view, icon = icon)
            canvas.text(text, font(view), textPosition, ColorBrush(colors.textColor))
        }

        when (textPosition) {
            null -> icon?.render(view, canvas, iconPosition(view, icon = icon))
            else -> icon?.render(view, canvas, iconPosition(view, icon = icon, stringPosition = textPosition))
        }
    }

    override fun stylesChanged(button: Button) {
        recalculateSize(button)

        super.stylesChanged(button)
    }

    override fun install(view: Button) {
        super.install(view)

        recalculateSize(view)
    }

    private fun recalculateSize(button: Button) {
        val icon   = icon(button)
        var size   = textMetrics.size(button.text, font(button))
        var width  = size.width
        var height = size.height

        if (icon != null) {
            val iconSize  = icon.size(button)
            val iconWidth = iconSize.width

            if (iconWidth > 0) {
                width += button.iconTextSpacing
            }

            width  += iconWidth
            height  = max(height, iconSize.height)
        }

        size = Size(width + insets.left + insets.right, height + insets.top + insets.bottom)

        button.idealSize = size

//        button.setSize(size)
    }
}
