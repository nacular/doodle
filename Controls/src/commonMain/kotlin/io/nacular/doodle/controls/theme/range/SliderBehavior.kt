package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.lerp

/**
 * Created by Nicholas Eddy on 2/13/18.
 */
public interface SliderBehavior<T>: Behavior<Slider<T>> where T: Comparable<T> {
    public fun Slider<T>.setFraction(value: Float) { fraction = value }
}

public abstract class AbstractSliderBehavior<T>(
        private val focusManager: FocusManager?
): SliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Comparable<T> {

    private val changed       : (Slider<T>, T,       T      ) -> Unit = { it,_,_ -> it.rerender() }
    private val enabledChanged: (View,      Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }
    private val styleChanged  : (View                       ) -> Unit = {           it.rerender() }
    private val oldPreferredSizes = mutableMapOf<View, (Size, Size) -> Size>()

    override fun install(view: Slider<T>) {
        view.changed              += changed
        view.keyChanged           += this
        view.styleChanged         += styleChanged
        view.pointerChanged       += this
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this

        oldPreferredSizes[view] = view.preferredSize
        view.preferredSize         = { min,_ -> Size(min.width, 16.0) }
    }

    override fun uninstall(view: Slider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.styleChanged         -= styleChanged
        view.pointerChanged       -= this
        view.enabledChanged       -= enabledChanged
        view.pointerMotionChanged -= this

        oldPreferredSizes.remove(view)?.let { view.preferredSize = it }
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as Slider<T>

        slider.fraction = fraction(slider, event.location)

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        ((event.source as? Slider<T>)?.handleKeyPress(event))
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as Slider<T>

        slider.fraction = fraction(slider, event.location)

        event.consume()
    }

    protected fun handlePosition(slider: Slider<T>): Double = when (slider.orientation) {
        Horizontal -> lerp(0.0,                                slider.width - handleSize(slider), slider.fraction)
        Vertical   -> lerp(slider.height - handleSize(slider), 0.0,                               slider.fraction)
    }

    protected fun handleSize(slider: Slider<T>): Double = when (slider.orientation) {
        Horizontal -> slider.height
        else       -> slider.width
    }

    private fun fraction(slider: Slider<T>, location: Point) = when (val sliderSize = size(slider)) {
        0.0  -> 0f
        else -> ((when (slider.orientation) {
            Horizontal -> location.x
            Vertical   -> slider.height - location.y
        } - handleSize(slider) / 2) / sliderSize).toFloat().coerceIn(0f ..1f)
    }

    private fun size(slider: Slider<T>): Double = when (slider.orientation) {
        Horizontal -> slider.width
        else       -> slider.height
    } - handleSize(slider)
}