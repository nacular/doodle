package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.range.size
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/13/18.
 */
public interface SliderBehavior<T>: Behavior<Slider<T>> where T: Number, T: Comparable<T> {
    public fun Slider<T>.set(to: Double) {
        this.set(to)
    }

    public fun Slider<T>.adjust(by: Double) {
        this.adjust(by)
    }

    public fun Slider<T>.set(range: ClosedRange<Double>) {
        this.set(range)
    }
}

public abstract class AbstractSliderBehavior<T>(
        private val focusManager: FocusManager?
): SliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Number, T: Comparable<T> {

    private lateinit var lastStart: T

    private val changed       : (Slider<T>, T,       T      ) -> Unit = { it,_,_ -> it.rerender() }
    private val enabledChanged: (View,      Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }
    private val styleChanged  : (View                       ) -> Unit = {           it.rerender() }

    protected var lastPointerPosition: Double = -1.0; private set

    override fun install(view: Slider<T>) {
        lastStart                  = view.value
        view.changed              += changed
        view.keyChanged           += this
        view.styleChanged         += styleChanged
        view.pointerChanged       += this
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: Slider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.styleChanged         -= styleChanged
        view.pointerChanged       -= this
        view.enabledChanged       -= enabledChanged
        view.pointerMotionChanged -= this
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider         = event.source as Slider<T>
        val scaleFactor    = scaleFactor(slider).let { if ( it != 0f) 1 / it else 0f }
        val handleSize     = handleSize    (slider)
        val handlePosition = handlePosition(slider)

        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        if (offset < handlePosition || offset > handlePosition + handleSize) {
            val handleCenter = handlePosition + handleSize / 2

            val adjustment = when (slider.orientation) {
                Horizontal -> offset       - handleCenter
                Vertical   -> handleCenter - offset
            }

            slider.adjust(by = scaleFactor * adjustment)
        }

        lastPointerPosition = offset
        lastStart           = slider.value

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun released(event: PointerEvent) {
        lastPointerPosition = -1.0
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        (event.source as? Slider<T>)?.let {
            lastStart = it.value
            handleKeyPress(it, event)
        }
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as Slider<T>

        val delta = when (slider.orientation) {
            Horizontal -> event.location.x    - lastPointerPosition
            Vertical   -> lastPointerPosition - event.location.y
        }

        slider.set(to = lastStart.toDouble() + delta / scaleFactor(slider))

        event.consume()
    }

    private fun scaleFactor(slider: Slider<T>): Float {
        val size = (if (slider.orientation === Horizontal) slider.width else slider.height) - handleSize(slider)

        return if (!slider.range.isEmpty()) (size / slider.range.size.toDouble()).toFloat() else 0f
    }

    protected fun handlePosition(slider: Slider<T>): Double = round((slider.value.toDouble() - slider.range.start.toDouble()) * scaleFactor(slider)).let {
        when (slider.orientation) {
            Horizontal -> it
            Vertical   -> slider.height - handleSize(slider) - it
        }
    }

    protected fun handleSize(slider: Slider<T>): Double = when (slider.orientation) {
        Horizontal -> slider.height
        else       -> slider.width
    }
}