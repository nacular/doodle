package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.controls.theme.range.AbstractCircularRangeSliderBehavior
import io.nacular.doodle.drawing.Canvas
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

/**
 * Creates a basic behavior for rendering [CircularRangeSlider]s. It allows customization of
 * the bar, range, thickness, and knobs at either end of the slider.
 *
 * @param barFill for the slider's background
 * @param startKnobFill for the knob at the start of the slider's range
 * @param endKnobFill for the knob at the end of the slider's range
 * @param rangeFill for the slider's range region
 * @param thickness of the slider
 * @param focusManager used to manage focus
 */
public class BasicCircularRangeSliderBehavior<T>(
    private val barFill      :  (CircularRangeSlider<T>) -> Paint,
    private val startKnobFill:  (CircularRangeSlider<T>) -> Paint,
    private val endKnobFill  :  (CircularRangeSlider<T>) -> Paint,
    private val rangeFill    : ((CircularRangeSlider<T>) -> Paint)? = endKnobFill,
    private val thickness    : Double = 20.0,
                focusManager : FocusManager? = null
): AbstractCircularRangeSliderBehavior<T>(focusManager) where T: Number, T: Comparable<T> {
    /**
     * Creates a basic behavior for rendering [CircularRangeSlider]s.
     *
     * @param barFill       for the slider's background
     * @param startKnobFill for the knob at the start of the slider's range
     * @param endKnobFill   for the knob at the end of the slider's range
     * @param rangeFill     for the slider's range region
     * @param thickness     of the slider
     * @param focusManager  used to manage focus
     */
    public constructor(
            barFill      : Paint         = Lightgray.paint,
            startKnobFill: Paint         = Blue.paint,
            endKnobFill  : Paint         = Blue.paint,
            rangeFill    : Paint?        = endKnobFill,
            thickness    : Double        = 20.0,
            focusManager : FocusManager? = null): this(barFill = { barFill }, startKnobFill = { startKnobFill }, endKnobFill = { endKnobFill }, rangeFill = rangeFill?.let { f -> { f } }, thickness, focusManager)

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

        canvas.path(ring(center, innerRadius, outerRadius), adjust(view, barFill(view)))

        rangeFill?.let {
            canvas.path(ringSection(
                center,
                innerRadius,
                outerRadius,
                start = startHandleAngle - if (startHandleAngle > endHandleAngle) _360 else _0,
                end   = endHandleAngle
            ), adjust(view, it(view)))
        }

        canvas.circle(Circle(startHandleCenter, (outerRadius - innerRadius) / 2), adjust(view, startKnobFill(view)))
        canvas.circle(Circle(endHandleCenter,   (outerRadius - innerRadius) / 2), adjust(view, endKnobFill  (view)))
    }

    private fun adjust(view: CircularRangeSlider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)

    public companion object {
        /**
         * Creates a basic behavior for rendering [CircularRangeSlider]s.
         *
         * @param barFill      for the slider's background
         * @param knobFill     for the knobs at the start/end of the slider's range
         * @param rangeFill    for the slider's range region
         * @param thickness    of the slider
         * @param focusManager used to manage focus
         */
        public inline operator fun <T> invoke(
            barFill      : Paint         = Lightgray.paint,
            knobFill     : Paint         = Blue.paint,
            rangeFill    : Paint?        = knobFill,
            thickness    : Double        = 20.0,
            focusManager : FocusManager? = null): BasicCircularRangeSliderBehavior<T> where T: Number, T: Comparable<T> {
            return BasicCircularRangeSliderBehavior(
                barFill       = barFill,
                startKnobFill = knobFill,
                endKnobFill   = knobFill,
                rangeFill     = rangeFill,
                thickness     = thickness,
                focusManager  = focusManager
            )
        }
    }
}