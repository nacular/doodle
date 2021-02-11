package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.theme.CircularSliderBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.Fill
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.ring
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.sin

public class BasicCircularSliderBehavior(
        private val barFill  : Fill,
        private val knobFill : Fill,
        private val thickness: Double = 20.0,
        focusManager: FocusManager? = null
): CircularSliderBehavior(focusManager) {
    public constructor(
            barColor    : Color         = Lightgray,
            knobColor   : Color         = Blue,
            thickness   : Double        = 20.0,
            focusManager: FocusManager? = null): this(barFill = ColorFill(barColor), knobFill = ColorFill(knobColor), thickness)

    override fun render(view: CircularSlider, canvas: Canvas) {
        val center      = Point(view.width / 2, view.height / 2)
        val outerRadius = minOf(view.width,     view.height) / 2
        val innerRadius = maxOf(0.0, outerRadius - thickness)

        canvas.path(ring(center, innerRadius, outerRadius), barFill)

        val handleAngle          = handleAngle(view)
        val radiusToHandleCenter = (innerRadius + outerRadius) / 2
        val handleCenter         = center + Point(radiusToHandleCenter * cos(handleAngle), radiusToHandleCenter * sin(handleAngle))

        canvas.circle(Circle(handleCenter, (outerRadius - innerRadius) / 2), knobFill)
    }
}
