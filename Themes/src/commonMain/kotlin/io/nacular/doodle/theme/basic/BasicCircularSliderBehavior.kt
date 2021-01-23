package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.theme.CircularSliderBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.path
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.times

public class BasicCircularSliderBehavior(
        private val barColor : Color = Lightgray,
        private val knobColor: Color = Blue,
        focusManager: FocusManager? = null
): CircularSliderBehavior(focusManager) {
    override fun render(view: CircularSlider, canvas: Canvas) {
        val center      = Point(view.width / 2, view.height / 2)
        val outerRadius = minOf(view.width, view.height) / 2
        val innerRadius = maxOf(0.0, outerRadius - 20)

        val outerEndPoint = center + Point(outerRadius * outerCloseCos, outerRadius * outerCloseSin)
        val innerEndPoint = center + Point(innerRadius * innerCloseCos, innerRadius * innerCloseSin)

        val path = path(Point(center.x, center.y - outerRadius)).arcTo(
                outerEndPoint,
                outerRadius,
                outerRadius,
                _0,
                largeArch = true,
                sweep     = true
        ).
        lineTo(Point(center.x, center.y - outerRadius)).
        lineTo(Point(center.x, center.y - innerRadius)).
        arcTo(
                innerEndPoint,
                innerRadius,
                innerRadius,
                _0,
                largeArch = true,
                sweep     = false
        ).lineTo(Point(center.x, center.y - innerRadius)).
        close()

        canvas.path(path, ColorFill(barColor))

        val handleAngle          = handleAngle(view)
        val radiusToHandleCenter = (innerRadius + outerRadius) / 2
        val handleCenter         = center + Point(radiusToHandleCenter * cos(handleAngle), radiusToHandleCenter * sin(handleAngle))

        canvas.circle(Circle(handleCenter, (outerRadius - innerRadius) / 2), ColorFill(knobColor))
    }

    private companion object {
        val outerCloseAngle = 269.9999 * degrees
        val innerCloseAngle = -89.9999 * degrees
        val outerCloseCos   = cos(outerCloseAngle)
        val outerCloseSin   = sin(outerCloseAngle)
        val innerCloseCos   = cos(innerCloseAngle)
        val innerCloseSin   = sin(innerCloseAngle)
    }
}
