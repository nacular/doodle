package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.atan
import io.nacular.measured.units.Measure
import io.nacular.measured.units.div
import io.nacular.measured.units.normalize
import io.nacular.measured.units.times
import kotlin.math.abs

public interface CircularSliderBehavior<T>: Behavior<CircularSlider<T>> where T: Comparable<T>

public abstract class AbstractCircularSliderBehavior<T>(
        private   val focusManager: FocusManager?,
        protected val startAngle  : Measure<Angle> = _270
): CircularSliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Comparable<T> {

    private lateinit var lastStart : T
    private val changed       : (CircularSlider<T>, T,       T      ) -> Unit = { it,_,_ -> it.rerender() }
    private val enabledChanged: (View,              Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }
    private val styleChanged  : (View                               ) -> Unit = {           it.rerender() }

    protected var lastPointerPosition: Measure<Angle> = _0; private set

    override fun install(view: CircularSlider<T>) {
        lastStart                  = view.value
        view.changed              += changed
        view.keyChanged           += this
        view.styleChanged         += styleChanged
        view.pointerChanged       += this
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: CircularSlider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.styleChanged         -= styleChanged
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
            slider.fraction = fraction(offset)
        }

        lastPointerPosition = offset
        lastStart           = slider.value

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        (event.source as? CircularSlider<T>)?.let {
            lastStart = it.value
            it.handleKeyPress(event)
        }
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as CircularSlider<T>
        val offset = pointerAngle(event)

        slider.fraction = fraction(offset)

        event.consume()
    }

    protected fun handleAngle(slider: CircularSlider<T>): Measure<Angle> = when {
        slider.range.isEmpty() -> startAngle
        else                   -> startAngle + slider.fraction * _360
    }.normalize()

    private fun fraction(offset: Measure<Angle>) = ((offset - startAngle).normalize() / _360).toFloat()

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
        public val   _0: Measure<Angle> get() = io.nacular.doodle.utils._0
        public val  _90: Measure<Angle> get() = io.nacular.doodle.utils._90
        public val _180: Measure<Angle> get() = io.nacular.doodle.utils._180
        public val _270: Measure<Angle> get() = io.nacular.doodle.utils._270
        public val _360: Measure<Angle> get() = io.nacular.doodle.utils._360
    }
}