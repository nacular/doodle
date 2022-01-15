package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.CircularSlider
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
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.atan
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.div
import io.nacular.measured.units.normalize
import io.nacular.measured.units.times
import kotlin.math.abs

public interface CircularSliderBehavior<T>: Behavior<CircularSlider<T>> where T: Number, T: Comparable<T> {
    public fun CircularSlider<T>.set(to: Double) {
        this.set(to)
    }

    public fun CircularSlider<T>.adjust(by: Double) {
        this.adjust(by)
    }

    public fun CircularSlider<T>.set(range: ClosedRange<Double>) {
        this.set(range)
    }
}

public abstract class AbstractCircularSliderBehavior<T>(
        private   val focusManager: FocusManager?,
        protected val startAngle  : Measure<Angle> = _270
): CircularSliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Number, T: Comparable<T> {

    private lateinit var lastStart : T
    private val changed       : (CircularSlider<T>, T,       T      ) -> Unit = { it,_,_ -> it.rerender() }
    private val enabledChanged: (View,              Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }

    protected var lastPointerPosition: Measure<Angle> = _0; private set

    override fun install(view: CircularSlider<T>) {
        lastStart                  = view.value
        view.changed              += changed
        view.keyChanged           += this
        view.pointerChanged       += this
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: CircularSlider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.enabledChanged       -= enabledChanged
        view.pointerMotionChanged -= this
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider      = event.source as CircularSlider<T>
        val offset      = pointerAngle(event)
        val handleAngle = handleAngle(slider)

        if (offset < handleAngle || offset > handleAngle) {
            slider.set((offset - startAngle).normalize() / _360 * slider.range.size.toDouble())
        }

        lastPointerPosition = offset
        lastStart           = slider.value

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider    = event.source as CircularSlider<T>
        val increment = slider.range.size.toDouble() / 100

        val (incrementKey, decrementKey) = when (slider.contentDirection) {
            LeftRight -> ArrowRight to ArrowLeft
            else      -> ArrowLeft  to ArrowRight
        }

        when (event.key) {
            ArrowUp,   incrementKey -> slider.adjust(by =  increment)
            ArrowDown, decrementKey -> slider.adjust(by = -increment)
        }
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as CircularSlider<T>
        val offset = pointerAngle(event)

        slider.set((offset - startAngle).normalize() / _360 * slider.range.size.toDouble())

        event.consume()
    }

    protected fun handleAngle(slider: CircularSlider<T>): Measure<Angle> = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + ((slider.value.toDouble() - slider.range.start.toDouble()) / slider.range.size.toDouble()) * _360
    }.normalize()

    private fun pointerAngle(event: PointerEvent): Measure<Angle> {
        @Suppress("UNCHECKED_CAST")
        val slider   = event.source as CircularSlider<T>
        val location = event.location - Point(slider.width / 2, slider.height / 2)

        return when {
            location.x < 0 && location.y < 0 -> atan(abs(location.y / location.x)) + _180
            location.x < 0                   -> atan(abs(location.x / location.y)) +  _90
            location.y < 0                   -> atan(abs(location.x / location.y)) + _270
            else                             -> atan(abs(location.y / location.x))
        }
    }

    protected companion object {
        public val   _0: Measure<Angle> =   0 * degrees
        public val  _90: Measure<Angle> =  90 * degrees
        public val _180: Measure<Angle> = 180 * degrees
        public val _270: Measure<Angle> = 270 * degrees
        public val _360: Measure<Angle> = 360 * degrees
    }
}