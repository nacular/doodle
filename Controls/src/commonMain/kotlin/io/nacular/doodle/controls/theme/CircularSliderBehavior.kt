package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.range.CircularSlider2
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

public typealias CircularSliderBehavior = CircularSliderBehavior2<Double>

public abstract class CircularSliderBehavior2<T>(
        private val focusManager: FocusManager?,
        private val startAngle  : Measure<Angle> = _270
): Behavior<CircularSlider2<T>>, PointerListener, PointerMotionListener, KeyListener where T: Number, T: Comparable<T> {

    private   lateinit var lastStart : T
    protected var lastPointerPosition: Measure<Angle> = _0
        private set

    private val changed: (CircularSlider2<T>, T, T) -> Unit = { it,_,_ -> it.rerender() }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }

    override fun install(view: CircularSlider2<T>) {
        lastStart                  = view.value
        view.changed              += changed
        view.keyChanged           += this
        view.pointerChanged       += this
        view.pointerMotionChanged += this
        view.enabledChanged       += enabledChanged
    }

    override fun uninstall(view: CircularSlider2<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.pointerMotionChanged -= this
        view.enabledChanged       -= enabledChanged
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider      = event.source as CircularSlider2<T>
        val offset      = pointerAngle(event)
        val handleAngle = handleAngle(slider)

        if (offset < handleAngle || offset > handleAngle) {
            slider.set((offset - startAngle).normalize().div(_360) * slider.range.size.toDouble())
        }

        lastPointerPosition = offset
        lastStart           = slider.value

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider    = event.source as CircularSlider2<T>
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
        val slider = event.source as CircularSlider2<T>
        val offset = pointerAngle(event)

        slider.set((offset - startAngle).normalize() / _360 * slider.range.size.toDouble())

        event.consume()
    }

    protected fun handleAngle(slider: CircularSlider2<T>): Measure<Angle> = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + ((slider.value.toDouble() - slider.range.start.toDouble()) / slider.range.size.toDouble()) * _360
    }.normalize()


    protected fun CircularSlider2<T>.set(to: Double) {
        this.set(to)
    }

    protected fun CircularSlider2<T>.adjust(by: Double) {
        this.adjust(by)
    }

    private fun pointerAngle(event: PointerEvent): Measure<Angle> {
        @Suppress("UNCHECKED_CAST")
        val slider   = event.source as CircularSlider2<T>
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