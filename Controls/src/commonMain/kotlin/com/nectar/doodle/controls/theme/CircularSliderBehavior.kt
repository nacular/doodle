package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.range.CircularSlider
import com.nectar.doodle.core.Behavior
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.KeyText.Companion.ArrowDown
import com.nectar.doodle.event.KeyText.Companion.ArrowLeft
import com.nectar.doodle.event.KeyText.Companion.ArrowRight
import com.nectar.doodle.event.KeyText.Companion.ArrowUp
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.event.PointerMotionListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.utils.size
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Angle.Companion.atan
import com.nectar.measured.units.Angle.Companion.degrees
import com.nectar.measured.units.Angle.Companion.normalize
import com.nectar.measured.units.Measure
import com.nectar.measured.units.div
import com.nectar.measured.units.times
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