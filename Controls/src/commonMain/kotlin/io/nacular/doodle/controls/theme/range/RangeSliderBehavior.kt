package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.RangeSlider
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

public interface RangeSliderBehavior<T>: Behavior<RangeSlider<T>> where T: Number, T: Comparable<T> {
    public fun RangeSlider<T>.set(to: ClosedRange<Double>) {
        this.set(to)
    }

    public fun RangeSlider<T>.adjust(startBy: Double, endBy: Double) {
        this.adjust(startBy, endBy)
    }

    public fun RangeSlider<T>.setLimits(range: ClosedRange<Double>) {
        this.setLimits(range)
    }
}

public abstract class AbstractRangeSliderBehavior<T>(
        private val focusManager: FocusManager?
): RangeSliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Number, T: Comparable<T> {

    private   lateinit var lastStart          : T
    private   lateinit var lastEnd            : T
    private            var draggingFirst      = false
    protected          var lastPointerPosition: Double = -1.0
        private set

    private val changed: (RangeSlider<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { it,_,_ -> it.rerender() }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }

    private val styleChanged: (View) -> Unit = { it.rerender() }

    override fun install(view: RangeSlider<T>) {
        lastStart                  = view.value.start
        lastEnd                    = view.value.endInclusive
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
        val slider      = event.source as RangeSlider<T>
        val scaleFactor = scaleFactor(slider).let { if ( it != 0f) 1 / it else 0f }

        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        val handleSize          = handleSize         (slider)
        val rangeSize           = rangeSize          (slider)
        val startHandlePosition = endHandlePosition  (slider)
        val endHandlePosition   = startHandlePosition(slider)

        draggingFirst = when (slider.orientation) {
            Horizontal -> offset < startHandlePosition + handleSize + rangeSize / 2
            Vertical   -> offset > startHandlePosition + handleSize + rangeSize / 2
        }

        when {
            draggingFirst -> {
                val handleCenter = startHandlePosition + handleSize / 2

                val adjustment = when (slider.orientation) {
                    Horizontal -> offset       - handleCenter
                    Vertical   -> handleCenter - offset
                }

                slider.adjust(startBy = scaleFactor * adjustment, endBy = 0.0)
            }
            else          -> {
                val handleCenter = endHandlePosition + handleSize / 2

                val adjustment = when (slider.orientation) {
                    Horizontal -> offset       - handleCenter
                    Vertical   -> handleCenter - offset
                }

                slider.adjust(startBy = 0.0, endBy = scaleFactor * adjustment)
            }
        }

        lastPointerPosition = offset
        lastStart           = slider.value.start
        lastEnd             = slider.value.endInclusive

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun released(event: PointerEvent) {
        lastPointerPosition = -1.0
    }

    override fun pressed(event: KeyEvent) {
        @Suppress("UNCHECKED_CAST")
        (event.source as? RangeSlider<T>)?.let {
            lastStart = it.value.start
            lastEnd   = it.value.endInclusive
            handleKeyPress(it, event)
        }
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as RangeSlider<T>

        val delta = when (slider.orientation) {
            Horizontal -> event.location.x    - lastPointerPosition
            Vertical   -> lastPointerPosition - event.location.y
        }

        when {
            draggingFirst -> slider.set(to = min(lastStart.toDouble() + delta / scaleFactor(slider), lastEnd.toDouble()) .. lastEnd.toDouble())
            else          -> slider.set(to = lastStart.toDouble() .. max(lastStart.toDouble(), lastEnd.toDouble() + delta / scaleFactor(slider)))
        }

        event.consume()
    }

    private fun scaleFactor(slider: RangeSlider<T>): Float {
        val size = (if (slider.orientation === Horizontal) slider.width else slider.height) - handleSize(slider)

        return if (!slider.range.isEmpty()) (size / slider.range.size.toDouble()).toFloat() else 0f
    }

    protected fun endHandlePosition  (slider: RangeSlider<T>): Double = adjustWhenVertical(slider, round((slider.value.start.toDouble       () - slider.range.start.toDouble()) * scaleFactor(slider)))
    protected fun startHandlePosition(slider: RangeSlider<T>): Double = adjustWhenVertical(slider, round((slider.value.endInclusive.toDouble() - slider.range.start.toDouble()) * scaleFactor(slider)))

    protected fun handleSize(slider: RangeSlider<T>): Double = if (slider.orientation === Horizontal) slider.height else slider.width
    protected fun rangeSize (slider: RangeSlider<T>): Double = startHandlePosition(slider) - endHandlePosition(slider)

    private fun adjustWhenVertical(slider: RangeSlider<T>, position: Double): Double = when (slider.orientation) {
        Horizontal -> position
        Vertical   -> slider.height - handleSize(slider) - position
    }
}