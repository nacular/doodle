package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.theme.CheckRadioButtonBehavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
class BasicRadioBehavior(textMetrics: TextMetrics): CheckRadioButtonBehavior<RadioButton>(textMetrics, BasicCheckBoxIcon, sSpacing) {

    private object BasicCheckBoxIcon: Icon<RadioButton> {

        override fun size(view: RadioButton) = Size(sCircleRadius * 2)

        override fun render(view: RadioButton, canvas: Canvas, at: Point) {
            val location  = at + Point(sCircleRadius, sCircleRadius)
            var fillColor = sFillColor

            var borderColor = sBorderColor
            var backgroundColor = if (view.model.armed && view.model.pointerOver) Color(0xaaaaaau) else sLightBGColor

            if (!view.enabled) {
                fillColor = sBorderColor
                borderColor = fillColor
                backgroundColor = sDisabledLightColor
            }

            canvas.circle(Circle(location, sCircleRadius).inset(0.5), Stroke(borderColor), ColorFill(backgroundColor))

            if (view.enabled && !view.model.armed && view.model.pointerOver) {
                canvas.circle(Circle(location, sCircleRadius - 1).inset(0.5), Stroke(sHoverColor, 2.0))
            }

            if (view.model.selected) {
                canvas.circle(Circle(location, sCircleRadius - 2).inset(0.5), ColorFill(fillColor))
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
