package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.theme.CheckRadioButtonBehavior
import com.nectar.doodle.core.Icon
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.ConvexPolygon
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
class BasicCheckBoxBehavior(textMetrics: TextMetrics): CheckRadioButtonBehavior<CheckBox>(textMetrics, BasicCheckBoxIcon, sSpacing) {

    private object BasicCheckBoxIcon: Icon<CheckBox> {

        override fun size(view: CheckBox) = Size(sRectSize)

        override fun render(view: CheckBox, canvas: Canvas, at: Point) {
            val size            = size(view)
            var fillColor       = sFillColor
            var borderColor     = sBorderColor
            var backgroundColor = if (view.model.armed && view.model.pointerOver) Color(0xaaaaaau); else sLightBGColor
            val rect            = Rectangle(at, size(view))

            if (!view.enabled) {
                fillColor       = Color(0x888888u)
                borderColor     = fillColor
                backgroundColor = sDisabledLightColor
            }

            canvas.rect(rect.inset(0.5), Pen(borderColor), ColorBrush(backgroundColor))

            if (view.enabled && !view.model.armed && view.model.pointerOver) {
                canvas.rect(rect.inset(2.0), Pen(sHoverColor, 2.0))
            }

            when {
                (view as CheckBox).indeterminate -> canvas.rect(Rectangle(at.x + 3, at.y + size.height / 2 - 1, size.width - 6, 2.0), ColorBrush(fillColor))
                view.model.selected              -> canvas.poly(ConvexPolygon(
                        Point(at.x + 3, at.y + 5),
                        Point(at.x + 5, at.y + 7),
                        Point(at.x + 9, at.y + 3),
                        Point(at.x + 9, at.y + 5),
                        Point(at.x + 5, at.y + 9),
                        Point(at.x + 3, at.y + 7)),
                        ColorBrush(fillColor))
            }
        }
    }

    companion object {
        // FIXME: Inject these colors instead
        private const val sSpacing            =  8.0
        private const val sRectSize           = 12.0
        private       val sFillColor          = Color(0x21a121u)
        private       val sHoverColor         = Color(0xfac55au)
        private       val sBorderColor        = Color(0x1c5180u)
        private       val sLightBGColor       = White
        private       val sDisabledLightColor = Color(0xccccccu)
    }
}
