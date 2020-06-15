package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.core.Behavior
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
import io.nacular.doodle.utils.size
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.atan
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.div
import io.nacular.measured.units.normalize
import io.nacular.measured.units.times
import kotlin.math.abs

abstract class CircularSliderBehavior(
        private val focusManager: FocusManager?,
        private val startAngle  : Measure<Angle> = _270): Behavior<CircularSlider>, PointerListener, PointerMotionListener, KeyListener {

    private   var lastStart           = -1.0
    protected var lastPointerPosition = _0
        private set

    private val changed: (CircularSlider, Double, Double) -> Unit = { it,_,_ -> it.rerender() }

    override fun install(view: CircularSlider) {
        view.changed              += changed
        view.keyChanged           += this
        view.pointerChanged       += this
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: CircularSlider) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.pointerMotionChanged -= this
    }

    override fun pressed(event: PointerEvent) {
        val slider      = event.source as CircularSlider
        val offset      = pointerAngle(event)
        val handleAngle = handleAngle(slider)

        if (offset < handleAngle || offset > handleAngle) {
            slider.value = (offset - startAngle).normalize().div(_360) * slider.range.size
        }

        lastPointerPosition = offset
        lastStart           = slider.value

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun keyPressed(event: KeyEvent) {
        val slider    = event.source as CircularSlider
        val increment = slider.range.size / 100

        when (event.key) {
            ArrowLeft,  ArrowDown -> slider.value -= increment
            ArrowRight, ArrowUp   -> slider.value += increment
        }
    }

    override fun dragged(event: PointerEvent) {
        val slider = event.source as CircularSlider
        val offset = pointerAngle(event)

        slider.value = (offset - startAngle).normalize() / _360 * slider.range.size

        event.consume()
    }

    protected fun handleAngle(slider: CircularSlider) = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + ((slider.value - slider.range.start) / slider.range.size) * _360
    }.normalize()

    private fun pointerAngle(event: PointerEvent): Measure<Angle> {
        val slider   = event.source as CircularSlider
        val location = event.location - Point(slider.width / 2, slider.height / 2)

        return when {
            location.x < 0 && location.y < 0 -> atan(abs(location.y / location.x)) + _180
            location.x < 0                   -> atan(abs(location.x / location.y)) +  _90
            location.y < 0                   -> atan(abs(location.x / location.y)) + _270
            else                             -> atan(abs(location.y / location.x))
        }
    }

    protected companion object {
        val   _0 =   0 * degrees
        val  _90 =  90 * degrees
        val _180 = 180 * degrees
        val _270 = 270 * degrees
        val _360 = 360 * degrees
    }
}