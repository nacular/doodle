package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.theme.range.AbstractCircularSliderBehavior
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
import io.nacular.doodle.theme.basic.range.TickLocation.GrooveAndRange
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times

/**
 * Creates a basic behavior for rendering [CircularSlider]s. It allows customization of
 * the bar, range, thickness, and knob of the slider.
 *
 * @param barFill for the slider's background
 * @param knobFill for the knob
 * @param rangeFill for the slider's range region
 * @param thickness of the slider
 * @param showTicks indicates if/how ticks should be drawn
 * @param startAngle where the slider's 0 position should be
 * @param focusManager used to manage focus
 */
public class BasicCircularSliderBehavior<T>(
    private val barFill     :  (CircularSlider<T>) -> Paint,
    private val knobFill    :  (CircularSlider<T>) -> Paint,
    private val rangeFill   : ((CircularSlider<T>) -> Paint)? = null,
    private val thickness   : Double                          = 20.0,
    private val showTicks   : CircularTickPresentation?       = null,
                startAngle  : Measure<Angle> = _270,
                focusManager: FocusManager?                   = null
): AbstractCircularSliderBehavior<T>(focusManager, startAngle) where T: Comparable<T> {
    /**
     * Creates a basic behavior for rendering [CircularSlider]s.
     *
     * @param barFill for the slider's background
     * @param knobFill for the knob
     * @param rangeFill for the slider's range region
     * @param thickness of the slider
     * @param showTicks indicates if/how ticks should be drawn
     * @param startAngle where the slider's 0 position should be
     * @param focusManager used to manage focus
     */
    public constructor(
        barFill     : Paint                     = Lightgray.paint,
        knobFill    : Paint                     = Blue.paint,
        rangeFill   : Paint?                    = null,
        thickness   : Double                    = 20.0,
        focusManager: FocusManager?             = null,
        showTicks   : CircularTickPresentation? = null,
        startAngle  : Measure<Angle>            = _270
    ): this(
        barFill      = { barFill  },
        knobFill     = { knobFill },
        rangeFill    = rangeFill?.let { f -> { f } },
        thickness    = thickness,
        showTicks    = showTicks,
        startAngle   = startAngle,
        focusManager = focusManager
    )

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: CircularSlider<T>, canvas: Canvas) {
        val center      = Point(view.width / 2, view.height / 2)
        val outerRadius = minOf(view.width,     view.height) / 2
        val innerRadius = maxOf(0.0, outerRadius - thickness)
        val handleAngle = handleAngle(view)

        val clipInfo = showTicks?.let { getCircularSnapClip(view.ticks, center, outerRadius, innerRadius, startAngle, it) }

        when (clipInfo) {
            null -> canvas.path(ring(center, innerRadius, outerRadius), adjust(view, barFill(view)))
            else -> canvas.clip(clipInfo.first) {
                path(ring(center, innerRadius, outerRadius), adjust(view, barFill(view)))
            }
        }

        rangeFill?.let {
            val drawPath = {
                canvas.path(ringSection(
                    center      = center,
                    innerRadius = innerRadius,
                    outerRadius = outerRadius,
                    start       = startAngle - if (startAngle > handleAngle) 360 * degrees else 0 * degrees,
                    end         = handleAngle,
                    startCap      = { _, point ->
                        arcTo(point, 1.0, 1.0, largeArch = true, sweep = true)
                    }
                ), adjust(view, it(view)))
            }

            when {
                clipInfo == null                     ->                               drawPath()
                showTicks.location == GrooveAndRange -> canvas.clip(clipInfo.first) { drawPath() }
            }
        }

        val radiusToHandleCenter = (innerRadius + outerRadius) / 2
        val handleCenter         = center + Point(radiusToHandleCenter * cos(handleAngle), radiusToHandleCenter * sin(handleAngle))

        canvas.circle(Circle(handleCenter, (outerRadius - innerRadius) / 2), adjust(view, knobFill(view)))
    }

    private fun adjust(view: CircularSlider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)

    public companion object {
        public operator fun <T: Comparable<T>> invoke(
            barFill     :  (CircularSlider<T>) -> Paint,
            knobFill    :  (CircularSlider<T>) -> Paint,
            rangeFill   : ((CircularSlider<T>) -> Paint)? = null,
            thickness   : Double        = 20.0,
            startAngle  : Measure<Angle> = _270,
            focusManager: FocusManager? = null
        ): BasicCircularSliderBehavior<T> = BasicCircularSliderBehavior<T>(
            barFill      = barFill,
            knobFill     = knobFill,
            rangeFill    = rangeFill,
            thickness    = thickness,
            startAngle   = startAngle,
            focusManager = focusManager
        )
    }
}