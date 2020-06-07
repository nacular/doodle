package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.range.CircularSlider
import com.nectar.doodle.controls.theme.CircularSliderBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Blue
import com.nectar.doodle.drawing.Color.Companion.Lightgray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.path
import com.nectar.measured.units.Angle.Companion.cos
import com.nectar.measured.units.Angle.Companion.degrees
import com.nectar.measured.units.Angle.Companion.sin
import com.nectar.measured.units.times

class BasicCircularSliderBehavior(
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

        canvas.path(path, ColorBrush(barColor))

        val handleAngle          = handleAngle(view)
        val radiusToHandleCenter = (innerRadius + outerRadius) / 2
        val handleCenter         = center + Point(radiusToHandleCenter * cos(handleAngle), radiusToHandleCenter * sin(handleAngle))

        canvas.circle(Circle(handleCenter, (outerRadius - innerRadius) / 2), ColorBrush(knobColor))
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
