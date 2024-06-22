package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.atan
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.div
import io.nacular.measured.units.normalize
import io.nacular.measured.units.times
import kotlin.math.abs

public interface CircularRangeSliderBehavior<T>: Behavior<CircularRangeSlider<T>> where T: Comparable<T>

public abstract class AbstractCircularRangeSliderBehavior<T>(
        private val focusManager: FocusManager?,
        private val startAngle  : Measure<Angle> = _270
): CircularRangeSliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Comparable<T> {

    private var lastEnd       = 0f
    private var lastStart     = 0f
    private var draggingFirst = false
    private val changed       : (CircularRangeSlider<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { it,_,_ -> it.rerender() }
    private val enabledChanged: (View,                   Boolean,        Boolean       ) -> Unit = { it,_,_ -> it.rerender() }
    private val styleChanged  : (View                                                  ) -> Unit = {           it.rerender() }

    private var lastPointerPosition: Point = Origin

    protected var lastPointerAngle: Measure<Angle> = _0; private set

    override fun install(view: CircularRangeSlider<T>) {
        lastStart                  = view.fraction.start
        lastEnd                    = view.fraction.endInclusive
        view.changed              += changed
        view.keyChanged           += this
        view.styleChanged         += styleChanged
        view.pointerChanged       += this
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: CircularRangeSlider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.styleChanged         -= styleChanged
        view.pointerChanged       -= this
        view.enabledChanged       -= enabledChanged
        view.pointerMotionChanged -= this
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider           = event.source as CircularRangeSlider<T>
        val offset           = pointerAngle(event) `as` degrees
        val startHandleAngle = startHandleAngle(slider)
        val rangeSweep       = rangeSweep(slider)
        val newValue         = fraction(offset)
        val delta            = (offset - startHandleAngle).normalize()

        draggingFirst = delta < rangeSweep / 2 || delta > (rangeSweep + (_360 - rangeSweep) / 2).normalize()

        when {
            draggingFirst -> {
                var start = newValue

                if (newValue > lastEnd) {
                    start = if (lastStart < lastEnd) 0f else lastEnd
                }

                slider.fraction = start .. slider.fraction.endInclusive
            }
            else          -> slider.fraction = slider.fraction.start .. newValue
        }

        lastPointerPosition = event.location - Point(event.source.width / 2, event.source.height / 2)
        lastPointerAngle    = offset
        lastStart           = slider.fraction.start
        lastEnd             = slider.fraction.endInclusive

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        (event.source as? CircularRangeSlider<T>)?.let {
            lastStart = it.fraction.start
            lastEnd   = it.fraction.endInclusive
            it.handleKeyPress(event)
        }
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider    = event.source as CircularRangeSlider<T>
        val newValue  = fraction(pointerAngle(event))
        val clockWise = clockwise(event.location - Point(event.source.width / 2, event.source.height / 2), lastPointerPosition)

        when {
            draggingFirst -> {
                var start = newValue

                when {
                    newValue > lastEnd -> start = if (clockWise) lastEnd else 0f
                    else               -> lastPointerPosition = event.location - Point(event.source.width / 2, event.source.height / 2)
                }

                slider.fraction = start .. lastEnd
            }
            else          -> {
                var end = newValue

                when {
                    newValue < lastStart -> end = if (!clockWise) lastStart else 1f
                    else                 -> lastPointerPosition = event.location - Point(event.source.width / 2, event.source.height / 2)
                }

                slider.fraction = lastStart .. end
            }
        }

        event.consume()
    }

    private fun fraction(offset: Measure<Angle>) = ((offset - startAngle).normalize() / _360).toFloat()

    protected fun startHandleAngle(slider: CircularRangeSlider<T>): Measure<Angle> = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + slider.fraction.start * _360
    }.normalize()

    protected fun endHandleAngle(slider: CircularRangeSlider<T>): Measure<Angle> = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + slider.fraction.endInclusive * _360
    }.normalize()

    private fun rangeSweep(slider: CircularRangeSlider<T>): Measure<Angle> = (endHandleAngle(slider) - startHandleAngle(slider)).normalize()

    private fun pointerAngle(event: PointerEvent): Measure<Angle> {
        @Suppress("UNCHECKED_CAST")
        val slider   = event.source as CircularRangeSlider<T>
        val location = event.location - Point(slider.width / 2, slider.height / 2)

        return when {
            location.x < 0 && location.y < 0 -> atan(abs(location.y / location.x)) + _180
            location.x < 0                   -> atan(abs(location.x / location.y)) +  _90
            location.y < 0                   -> atan(abs(location.x / location.y)) + _270
            else                             -> atan(abs(location.y / location.x))
        }
    }

    /*
     * Given two 2D vector A = a1x + a2y + 0z and B = b1x + b2y + 0z.
     * Then cross product is calculated as =
     * (a2 * b3 – a3 * b2)x + (a3 * b1 – a1 * b3)y + (a1 * b2 – a2 * b1)z
     * where [(a2 * b3 – a3 * b2), (a3 * b1 – a1 * b3), (a1 * b2 – a2 * b1)] are the coefficient of unit vector along x, y and z directions.
     */
    private fun clockwise(first: Point, second: Point): Boolean = (first.x * second.y - first.y * second.x) < 0

    protected companion object {
        public val   _0: Measure<Angle> get() = io.nacular.doodle.utils._0
        public val  _90: Measure<Angle> get() = io.nacular.doodle.utils._90
        public val _180: Measure<Angle> get() = io.nacular.doodle.utils._180
        public val _270: Measure<Angle> get() = io.nacular.doodle.utils._270
        public val _360: Measure<Angle> get() = io.nacular.doodle.utils._360
    }
}