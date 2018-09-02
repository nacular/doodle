package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.theme.AbstractButtonUI
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
class BasicButtonUI(
        private val textMetrics        : TextMetrics,
        private val backgroundColor    : Color,
        private val darkBackgroundColor: Color,
        private val foregroundColor    : Color,
        private val borderColor        : Color,
        private val borderWidth        : Double = 1.0): AbstractButtonUI(textMetrics) {

    protected var insets = Insets(4.0)

    override fun render(gizmo: Button, canvas: Canvas) {
        val model       = gizmo.model
        var fillColor   = if (model.selected || model.pressed && model.armed) darkBackgroundColor else gizmo.backgroundColor ?: backgroundColor
        var textColor   = gizmo.foregroundColor ?: foregroundColor
        var borderColor = borderColor

        if (!gizmo.enabled) {
            textColor   = textColor.lighter  ()
            fillColor   = fillColor.lighter  ()
            borderColor = borderColor.lighter()
        }

        val penWidth = if (gizmo.enabled && (model.pressed || model.mouseOver)) 2 * borderWidth else borderWidth

        canvas.rect(Rectangle(size = gizmo.size).inset(penWidth / 2), Pen(borderColor, penWidth), ColorBrush(fillColor))

        val icon         = icon(gizmo)
        val text         = gizmo.text
        val textPosition = textPosition(gizmo, icon)

        if (text.isNotBlank()) {
            canvas.text(text, font(gizmo), textPosition, ColorBrush(textColor))
        }

        icon?.render(gizmo, canvas, iconPosition(gizmo, icon))
    }

    override fun install(gizmo: Button) {
        super.install(gizmo)

        recalculateSize(gizmo)
    }

    private fun recalculateSize(gizmo: Button) {
        val icon   = if (gizmo.enabled) gizmo.icon else gizmo.disabledIcon
        var size   = textMetrics.size(gizmo.text, font(gizmo))
        var width  = size.width
        var height = size.height

        if (icon != null) {
            val iconWidth = icon.size.width

            if (iconWidth > 0) {
                width += gizmo.iconTextSpacing
            }

            width  += iconWidth
            height  = max(height, icon.size.height)
        }

        size = Size(width + insets.left + insets.right, height + insets.top + insets.bottom)

        gizmo.idealSize = size

//        gizmo.setSize(size)
    }
}
