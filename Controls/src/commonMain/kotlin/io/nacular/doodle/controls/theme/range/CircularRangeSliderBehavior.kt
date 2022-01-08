package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.controls.range.size
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowLeft
import io.nacular.doodle.event.KeyText.Companion.ArrowRight
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
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

public interface CircularRangeSliderBehavior<T>: Behavior<CircularRangeSlider<T>> where T: Number, T: Comparable<T> {
    public fun CircularRangeSlider<T>.set(to: ClosedRange<Double>) {
        this.set(to)
    }

    public fun CircularRangeSlider<T>.adjust(startBy: Double, endBy: Double) {
        this.adjust(startBy, endBy)
    }

    public fun CircularRangeSlider<T>.setLimits(range: ClosedRange<Double>) {
        this.setLimits(range)
    }
}

public abstract class AbstractCircularRangeSliderBehavior<T>(
        private val focusManager: FocusManager?,
        private val startAngle  : Measure<Angle> = /*_0 / */_270
): CircularRangeSliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Number, T: Comparable<T> {

    private lateinit var lastEnd       : T
    private lateinit var lastStart     : T
    private          var draggingFirst   = false
    private          val changed       : (CircularRangeSlider<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { it,_,_ -> it.rerender() }
    private          val enabledChanged: (View,                   Boolean,        Boolean       ) -> Unit = { it,_,_ -> it.rerender() }

    private var lastPointerPosition: Point = Origin

    protected var lastPointerAngle: Measure<Angle> = _0; private set

    override fun install(view: CircularRangeSlider<T>) {
        lastStart                  = view.value.start
        lastEnd                    = view.value.endInclusive
        view.changed              += changed
        view.keyChanged           += this
        view.pointerChanged       += this
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: CircularRangeSlider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
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
        val newValue         = (offset - startAngle      ).normalize() / _360 * slider.range.size.toDouble()
        val delta            = (offset - startHandleAngle).normalize()

        draggingFirst = delta < rangeSweep / 2 || delta > (rangeSweep + (_360 - rangeSweep) / 2).normalize()

        when {
            draggingFirst -> {
                var start = newValue

                if (newValue > lastEnd.toDouble()) {
                    start = if (lastStart < lastEnd) 0.0 else lastEnd.toDouble()
                }

                slider.set(start .. slider.value.endInclusive.toDouble())
            }
            else          -> slider.set(slider.value.start.toDouble() .. newValue)
        }

        lastPointerPosition = event.location - Point(event.source.width / 2, event.source.height / 2)
        lastPointerAngle    = offset
        lastStart           = slider.value.start
        lastEnd             = slider.value.endInclusive

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider    = event.source as CircularRangeSlider<T>
        val increment = slider.range.size.toDouble() / 100

        val (incrementKey, decrementKey) = when (slider.contentDirection) {
            LeftRight -> ArrowRight to ArrowLeft
            else      -> ArrowLeft  to ArrowRight
        }

        when (event.key) {
            ArrowUp,   incrementKey -> slider.adjust(startBy =  increment, endBy =  increment)
            ArrowDown, decrementKey -> slider.adjust(startBy = -increment, endBy = -increment)
        }
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider    = event.source as CircularRangeSlider<T>
        val newValue  = (pointerAngle(event) - startAngle).normalize() / _360 * slider.range.size.toDouble()
        val clockWise = clockwise(event.location - Point(event.source.width / 2, event.source.height / 2), lastPointerPosition)

        when {
            draggingFirst -> {
                var start = newValue

                when {
                    newValue > lastEnd.toDouble() -> start = if (clockWise) lastEnd.toDouble() else slider.range.start.toDouble()
                    else                          -> lastPointerPosition = event.location - Point(event.source.width / 2, event.source.height / 2)
                }

                slider.set(start .. lastEnd.toDouble())
            }
            else          -> {
                var end = newValue

                when {
                    newValue < lastStart.toDouble() -> end = if (!clockWise) lastStart.toDouble() else slider.range.endInclusive.toDouble()
                    else                            -> lastPointerPosition = event.location - Point(event.source.width / 2, event.source.height / 2)
                }

                slider.set(lastStart.toDouble() .. end)
            }
        }

        event.consume()
    }

    protected fun startHandleAngle(slider: CircularRangeSlider<T>): Measure<Angle> = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + ((slider.value.start.toDouble() - slider.range.start.toDouble()) / slider.range.size.toDouble()) * _360
    }.normalize()

    protected fun endHandleAngle(slider: CircularRangeSlider<T>): Measure<Angle> = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + ((slider.value.endInclusive.toDouble() - slider.range.start.toDouble()) / slider.range.size.toDouble()) * _360
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
        public val   _0: Measure<Angle> =   0 * degrees
        public val  _90: Measure<Angle> =  90 * degrees
        public val _180: Measure<Angle> = 180 * degrees
        public val _270: Measure<Angle> = 270 * degrees
        public val _360: Measure<Angle> = 360 * degrees
    }
}