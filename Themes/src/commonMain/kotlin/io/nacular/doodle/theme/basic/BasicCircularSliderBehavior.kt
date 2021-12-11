package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.range.CircularSlider2
import io.nacular.doodle.controls.theme.CircularSliderBehavior2
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.ring
import io.nacular.doodle.theme.PaintMapper
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.sin

public typealias BasicCircularSliderBehavior = BasicCircularSliderBehavior2<Double>

public class BasicCircularSliderBehavior2<T>(
        private val barFill  : Paint,
        private val knobFill : Paint,
        private val thickness: Double = 20.0,
        focusManager: FocusManager? = null
): CircularSliderBehavior2<T>(focusManager) where T: Number, T: Comparable<T> {
    public constructor(
            barColor    : Color         = Lightgray,
            knobColor   : Color         = Blue,
            thickness   : Double        = 20.0,
            focusManager: FocusManager? = null): this(barFill = ColorPaint(barColor), knobFill = ColorPaint(knobColor), thickness, focusManager)

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: CircularSlider2<T>, canvas: Canvas) {
        val center      = Point(view.width / 2, view.height / 2)
        val outerRadius = minOf(view.width,     view.height) / 2
        val innerRadius = maxOf(0.0, outerRadius - thickness)

        canvas.path(ring(center, innerRadius, outerRadius), adjust(view, barFill))

        val handleAngle          = handleAngle(view)
        val radiusToHandleCenter = (innerRadius + outerRadius) / 2
        val handleCenter         = center + Point(radiusToHandleCenter * cos(handleAngle), radiusToHandleCenter * sin(handleAngle))

        canvas.circle(Circle(handleCenter, (outerRadius - innerRadius) / 2), adjust(view, knobFill))
    }

    private fun adjust(view: CircularSlider2<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)
}