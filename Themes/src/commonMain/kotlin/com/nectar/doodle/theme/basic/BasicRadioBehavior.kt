package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.theme.CheckRadioButtonBehavior
import com.nectar.doodle.core.Icon
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
class BasicRadioBehavior(textMetrics: TextMetrics): CheckRadioButtonBehavior(textMetrics, BasicCheckBoxIcon, sSpacing) {

    private object BasicCheckBoxIcon: Icon<Button> {

        override val size = Size(sCircleRadius * 2)

        override fun render(view: Button, canvas: Canvas, at: Point) {
            val location  = at + Point(sCircleRadius, sCircleRadius)
            var fillColor = sFillColor

            var borderColor = sBorderColor
            var backgroundColor = if (view.model.armed && view.model.pointerOver) Color(0xaaaaaau) else sLightBGColor

            if (!view.enabled) {
                fillColor = sBorderColor
                borderColor = fillColor
                backgroundColor = sDisabledLightColor
            }

            canvas.circle(Circle(location, sCircleRadius).inset(0.5), Pen(borderColor), ColorBrush(backgroundColor))

            if (view.enabled && !view.model.armed && view.model.pointerOver) {
                canvas.circle(Circle(location, sCircleRadius - 1).inset(0.5), Pen(sHoverColor, 2.0))
            }

            if (view.model.selected) {
                canvas.circle(Circle(location, sCircleRadius - 2).inset(0.5), ColorBrush(fillColor))
            }
        }
    }

    companion object {
        private const val sSpacing            = 8.0
        private       val sFillColor          = Color(0x21a121u)
        private       val sHoverColor         = Color(0xfac55au)
        private       val sBorderColor        = Color(0x1c5180u)
        private       val sLightBGColor       = White
        private const val sCircleRadius       = 6.0
        private       val sDisabledLightColor = Color(0xccccccu)
    }
}
