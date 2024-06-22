package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.RangeSlider
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.lerp
import kotlin.math.max
import kotlin.math.min

public interface RangeSliderBehavior<T>: Behavior<RangeSlider<T>> where T: Comparable<T> {
    public var RangeSlider<T>.fraction: ClosedRange<Float> get() = fraction; set(new) { fraction = new }

    @Deprecated("Use fraction instead")
    public fun <A> RangeSlider<A>.set(to: ClosedRange<Double>) where A : Comparable<A>, A: Number {
        fraction = ((to.start - range.start.toDouble()) / (range.endInclusive.toDouble() - range.start.toDouble())).toFloat() ..
                ((to.endInclusive - range.start.toDouble()) / (range.endInclusive.toDouble() - range.start.toDouble())).toFloat()
    }

    @Deprecated("Use fraction instead")
    public fun <A> RangeSlider<A>.adjust(startBy: Double, endBy: Double) where A : Comparable<A>, A: Number {
        set(value.start.toDouble() + startBy .. value.endInclusive.toDouble() + endBy)
    }

    @Deprecated("Will be removed soon")
    public fun <T> RangeSlider<T>.setLimits(range: ClosedRange<Double>) where T: Number, T: Comparable<T> {
        @Suppress("UNCHECKED_CAST")
        when (model.limits.start) {
            is Int    -> model.limits = (range.start.toInt() .. range.endInclusive.toInt()                    ) as ClosedRange<T>
            is Float  -> model.limits = (range.start.toFloat() .. range.endInclusive.toFloat()                ) as ClosedRange<T>
            is Double -> model.limits = (range.start .. range.endInclusive                                    ) as ClosedRange<T>
            is Long   -> model.limits = (range.start.toLong() .. range.endInclusive.toLong()                  ) as ClosedRange<T>
            is Short  -> model.limits = (range.start.toInt().toShort() .. range.endInclusive.toInt().toShort()) as ClosedRange<T>
            is Byte   -> model.limits = (range.start.toInt().toByte() .. range.endInclusive.toInt().toByte()  ) as ClosedRange<T>
        }
    }
}

public abstract class AbstractRangeSliderBehavior<T>(
        private val focusManager: FocusManager?
): RangeSliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Comparable<T> {

    private var draggingFirst = false

    private val changed: (RangeSlider<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { it,_,_ -> it.rerender() }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }

    private val styleChanged: (View) -> Unit = { it.rerender() }

    override fun install(view: RangeSlider<T>) {
        view.changed              += changed
        view.keyChanged           += this
        view.styleChanged         += styleChanged
        view.pointerChanged       += this
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: RangeSlider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.pointerMotionChanged -= this
        view.enabledChanged       -= enabledChanged
        view.styleChanged         -= styleChanged
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as RangeSlider<T>
        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        val handleSize          = handleSize         (slider)
        val rangeSize           = rangeSize          (slider)
        val startHandlePosition = startHandlePosition(slider)

        draggingFirst = when (slider.orientation) {
            Horizontal -> offset < startHandlePosition + handleSize + rangeSize / 2
            Vertical   -> offset > startHandlePosition + handleSize + rangeSize / 2
        }

        updateFraction(slider, event, draggingFirst)

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        ((event.source as? RangeSlider<T>)?.handleKeyPress(event))
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        updateFraction(event.source as RangeSlider<T>, event, draggingFirst)

        event.consume()
    }

    protected fun startHandlePosition(slider: RangeSlider<T>): Double = handlePosition(slider, slider.fraction.start)

    protected fun endHandlePosition  (slider: RangeSlider<T>): Double = handlePosition(slider, slider.fraction.endInclusive)

    protected fun handleSize(slider: RangeSlider<T>): Double = when (slider.orientation) {
        Horizontal -> slider.height
        else       -> slider.width
    }

    protected fun rangeSize(slider: RangeSlider<T>): Double = endHandlePosition(slider) - startHandlePosition(slider)

    protected fun handlePosition(slider: RangeSlider<T>, fraction: Float): Double = when (slider.orientation) {
        Horizontal -> lerp(0.0,                                slider.width - handleSize(slider), fraction)
        Vertical   -> lerp(slider.height - handleSize(slider), 0.0,                               fraction)
    }

    private fun updateFraction(slider: RangeSlider<T>, event: PointerEvent, draggingFirst: Boolean) {
        when {
            draggingFirst -> slider.fraction = min(slider.fraction.endInclusive, fraction(slider, event.location)) .. slider.fraction.endInclusive
            else          -> slider.fraction = slider.fraction.start .. max(slider.fraction.start, fraction(slider, event.location))
        }
    }

    private fun fraction(slider: RangeSlider<T>, location: Point) = when (val sliderSize = size(slider)) {
        0.0  -> 0f
        else -> ((when (slider.orientation) {
            Horizontal -> location.x
            Vertical   -> slider.height - location.y
        } - handleSize(slider) / 2) / sliderSize).toFloat().coerceIn(0f .. 1f)
    }

    private fun size(slider: RangeSlider<T>): Double = when (slider.orientation) {
        Horizontal -> slider.width
        else       -> slider.height
    } - handleSize(slider)
}