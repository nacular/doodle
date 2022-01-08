package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.controls.theme.range.AbstractCircularRangeSliderBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.ring
import io.nacular.doodle.geometry.ringSection
import io.nacular.doodle.theme.PaintMapper
import io.nacular.doodle.theme.basic.defaultDisabledPaintMapper
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.sin

public class BasicCircularRangeSliderBehavior<T>(
        private val barFill  : Paint,
        private val knobFill : Paint,
        private val rangeFill: Paint? = knobFill,
        private val thickness: Double = 20.0,
        focusManager: FocusManager? = null
): AbstractCircularRangeSliderBehavior<T>(focusManager) where T: Number, T: Comparable<T> {
    public constructor(
            barColor    : Color         = Lightgray,
            knobColor   : Color         = Blue,
            rangeColor  : Color?        = knobColor,
            thickness   : Double        = 20.0,
            focusManager: FocusManager? = null): this(barFill = barColor.paint, knobFill = knobColor.paint, rangeFill = rangeColor?.paint, thickness, focusManager)

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: CircularRangeSlider<T>, canvas: Canvas) {
        val center               = Point(view.width / 2, view.height / 2)
        val outerRadius          = minOf(view.width,     view.height) / 2
        val innerRadius          = maxOf(0.0, outerRadius - thickness)
        val radiusToHandleCenter = (innerRadius + outerRadius) / 2
        val startHandleAngle     = startHandleAngle(view)
        val startHandleCenter    = center + Point(radiusToHandleCenter * cos(startHandleAngle), radiusToHandleCenter * sin(startHandleAngle))
        val endHandleAngle       = endHandleAngle(view)
        val endHandleCenter      = center + Point(radiusToHandleCenter * cos(endHandleAngle), radiusToHandleCenter * sin(endHandleAngle))

        canvas.path(ring(center, innerRadius, outerRadius), adjust(view, barFill))

        rangeFill?.let {
            canvas.path(ringSection(
                center,
                innerRadius,
                outerRadius,
                start = startHandleAngle - if (startHandleAngle > endHandleAngle) _360 else _0,
                end   = endHandleAngle
            ), adjust(view, it))
        }

        canvas.circle(Circle(startHandleCenter, (outerRadius - innerRadius) / 2), adjust(view, knobFill))
        canvas.circle(Circle(endHandleCenter,   (outerRadius - innerRadius) / 2), adjust(view, knobFill))
    }

    private fun adjust(view: CircularRangeSlider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)
}