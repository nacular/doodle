package io.nacular.doodle.controls.theme.range

import io.nacular.doodle.controls.range.RangeSlider
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

    override fun install(view: RangeSlider<T>) {
        lastStart                  = view.value.start
        lastEnd                    = view.value.endInclusive
        view.changed              += changed
        view.keyChanged           += this
        view.pointerChanged       += this
        view.pointerMotionChanged += this
        view.enabledChanged       += enabledChanged
    }

    override fun uninstall(view: RangeSlider<T>) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.pointerMotionChanged -= this
        view.enabledChanged       -= enabledChanged
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider      = event.source as RangeSlider<T>
        val scaleFactor = scaleFactor(slider).let { if ( it != 0f) 1 / it else 0f }

        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        val barSize          = barSize         (slider)
        val rangeSize        = rangeSize       (slider)
        val startBarPosition = startBarPosition(slider)
        val endBarPosition   = endBarPosition  (slider)

        draggingFirst = offset < startBarPosition + barSize + rangeSize / 2

        when {
            draggingFirst -> slider.adjust(startBy = scaleFactor * (offset - (startBarPosition + barSize / 2)), endBy = 0.0)
            else          -> slider.adjust(startBy = 0.0, endBy = scaleFactor * (offset - (endBarPosition + barSize / 2)))
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
        val slider    = event.source as RangeSlider<T>
        lastStart     = slider.value.start
        lastEnd       = slider.value.endInclusive
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
        val slider = event.source as RangeSlider<T>

        val delta = when (slider.orientation) {
            Horizontal -> event.location.x - lastPointerPosition
            Vertical   -> event.location.y - lastPointerPosition
        }

        when {
            draggingFirst -> slider.set(to = min(lastStart.toDouble() + delta / scaleFactor(slider), lastEnd.toDouble()) .. lastEnd.toDouble())
            else          -> slider.set(to = lastStart.toDouble() .. max(lastStart.toDouble(), lastEnd.toDouble() + delta / scaleFactor(slider)))
        }

        event.consume()
    }

    private fun scaleFactor(slider: RangeSlider<T>): Float {
        val size = (if (slider.orientation === Horizontal) slider.width else slider.height) - barSize(slider)

        return if (!slider.range.isEmpty()) (size / slider.range.size.toDouble()).toFloat() else 0f
    }

    protected fun startBarPosition(slider: RangeSlider<T>): Double = round((slider.value.start.toDouble()        - slider.range.start.toDouble()) * scaleFactor(slider))
    protected fun endBarPosition  (slider: RangeSlider<T>): Double = round((slider.value.endInclusive.toDouble() - slider.range.start.toDouble()) * scaleFactor(slider))

    protected fun barSize  (slider: RangeSlider<T>): Double = if (slider.orientation === Horizontal) slider.height else slider.width
    protected fun rangeSize(slider: RangeSlider<T>): Double = endBarPosition(slider) - startBarPosition(slider)
}