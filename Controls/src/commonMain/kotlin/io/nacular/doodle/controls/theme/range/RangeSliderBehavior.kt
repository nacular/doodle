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
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Cancelable
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.NoOpCompletable
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.lerp
import kotlin.math.max
import kotlin.math.min

public interface RangeSliderBehavior<T>: Behavior<RangeSlider<T>> where T: Comparable<T> {
    /**
     * The bounding [Rectangle] of the start handle used to adjust [slider]. This defaults
     * to `slider.bounds.atOrigin`.
     */
    public fun startHandleBounds(slider: RangeSlider<T>): Rectangle = slider.bounds.atOrigin

    /**
     * The bounding [Rectangle] of the end handle used to adjust [slider]. This defaults
     * to `slider.bounds.atOrigin`.
     */
    public fun endHandleBounds(slider: RangeSlider<T>): Rectangle = slider.bounds.atOrigin
}


/**
 * Convenient base class providing basic [RangeSliderBehavior] functionality.
 */
public abstract class AbstractRangeSliderBehavior<T>(
    private val focusManager     : FocusManager?,
    private val animateHandleMove: (block: (progress: Float) -> Unit) -> Completable = { NoOpCompletable.also { it(1f) } }
): RangeSliderBehavior<T>, PointerListener, PointerMotionListener, KeyListener where T: Comparable<T> {

    private class AnimationInfo(
        var startHandleHPosition: Double,
        var startHandleVPosition: Double,
        var startProgress       : Float,
        var endHandleHPosition  : Double,
        var endHandleVPosition  : Double,
        var endProgress         : Float
    ) {
        var completable: Cancelable? by autoCanceling(null)
    }

    private var draggingFirst = false

    private val changed: (RangeSlider<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { it,_,_ -> it.rerender() }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { it,_,_ -> it.rerender() }

    private val styleChanged: (View) -> Unit = { it.rerender() }

    private val animationInfo = mutableMapOf<RangeSlider<T>, AnimationInfo>()

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

        val info = animationInfo[slider]?.apply {
            startHandleHPosition = startHandlePosition(slider, Horizontal)
            startHandleVPosition = startHandlePosition(slider, Vertical  )
            startProgress        = 0f
            endHandleHPosition   = endHandlePosition(slider, Horizontal)
            endHandleVPosition   = endHandlePosition(slider, Vertical  )
            endProgress          = 0f
        } ?: AnimationInfo(
            startHandlePosition(slider, Horizontal),
            startHandlePosition(slider, Vertical  ),
            0f,
            endHandlePosition(slider, Horizontal),
            endHandlePosition(slider, Vertical  ),
            0f
        ).also { animationInfo[slider] = it }

        updateFraction(slider, event, draggingFirst)

        info.completable = animateHandleMove {
            when {
                draggingFirst -> animationInfo[slider]?.startProgress = it
                else          -> animationInfo[slider]?.endProgress   = it
            }
            slider.rerender()
        }.apply {
            completed += {
                animationInfo.remove(slider)
            }
        }

        focusManager?.requestFocus(slider)

        event.consume()

        when {
            draggingFirst -> startHandlePressed(slider)
            else          -> endHandlePressed  (slider)
        }
    }

    override fun released(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val slider = event.source as RangeSlider<T>

        when {
            draggingFirst -> startHandleReleased(slider)
            else          -> endHandleReleased  (slider)
        }
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

    override fun startHandleBounds(slider: RangeSlider<T>): Rectangle {
        val handleSize     = handleSize         (slider)
        val handlePosition = startHandlePosition(slider)

        return when (slider.orientation) {
            Horizontal -> Rectangle(handlePosition, 0.0, handleSize, handleSize)
            else       -> Rectangle(0.0, handlePosition, handleSize, handleSize)
        }
    }

    override fun endHandleBounds(slider: RangeSlider<T>): Rectangle {
        val handleSize     = handleSize       (slider)
        val handlePosition = endHandlePosition(slider)

        return when (slider.orientation) {
            Horizontal -> Rectangle(handlePosition, 0.0, handleSize, handleSize)
            else       -> Rectangle(0.0, handlePosition, handleSize, handleSize)
        }
    }

    /**
     * @param slider to get start-handle position for
     * @return the slider's start-handle position along it's length
     */
    protected fun startHandlePosition(slider: RangeSlider<T>): Double = startHandlePosition(slider, slider.orientation)

    /**
     * @param slider to get end-handle position for
     * @return the slider's end-handle position along it's length
     */
    protected fun endHandlePosition(slider: RangeSlider<T>): Double = endHandlePosition(slider, slider.orientation)

    /**
     * @param slider to get handle position for
     * @param fraction the handle is at: `0-1`
     * @return the slider's handle position along it's length
     */
    protected fun handlePosition(slider: RangeSlider<T>, fraction: Float): Double = handlePosition(slider, fraction, slider.orientation)

    /**
     * @param slider to get handle size for
     * @return the handle's length perpendicular to the slider length
     */
    protected fun handleSize(slider: RangeSlider<T>): Double = when (slider.orientation) {
        Horizontal -> slider.height
        else       -> slider.width
    }

    /**
     * @param slider to get range size for
     * @return the range's length along the slider length
     */
    protected fun rangeSize(slider: RangeSlider<T>): Double = endHandlePosition(slider) - startHandlePosition(slider)

    /**
     * Event indicating the slider's start-handle has been pressed/activated with the Pointer.
     *
     * @param slider being acted on
     */
    protected open fun startHandlePressed(slider: RangeSlider<T>) {}

    /**
     * Event indicating the slider's start-handle is no longer being pressed/activated with the Pointer.
     *
     * @param slider being acted on
     */
    protected open fun startHandleReleased(slider: RangeSlider<T>) {}

    /**
     * Event indicating the slider's end-handle has been pressed/activated with the Pointer.
     *
     * @param slider being acted on
     */
    protected open fun endHandlePressed(slider: RangeSlider<T>) {}

    /**
     * Event indicating the slider's end-handle is no longer being pressed/activated with the Pointer.
     *
     * @param slider being acted on
     */
    protected open fun endHandleReleased(slider: RangeSlider<T>) {}

    private fun startHandlePosition(slider: RangeSlider<T>, orientation: Orientation): Double = when (val info = animationInfo[slider]) {
        null -> handlePosition(slider, slider.fraction.start, orientation)
        else -> when (orientation) {
            Horizontal -> lerp(info.startHandleHPosition, lerp(0.0, slider.width - handleSize(slider),  slider.fraction.start), info.startProgress)
            Vertical   -> lerp(info.startHandleVPosition, lerp(slider.height - handleSize(slider), 0.0, slider.fraction.start), info.startProgress)
        }
    }

    private fun endHandlePosition(slider: RangeSlider<T>, orientation: Orientation): Double = when (val info = animationInfo[slider]) {
        null -> handlePosition(slider, slider.fraction.endInclusive, orientation)
        else -> when (orientation) {
            Horizontal -> lerp(info.endHandleHPosition, lerp(0.0, slider.width - handleSize(slider),  slider.fraction.endInclusive), info.endProgress)
            Vertical   -> lerp(info.endHandleVPosition, lerp(slider.height - handleSize(slider), 0.0, slider.fraction.endInclusive), info.endProgress)
        }
    }

    private fun handlePosition(slider: RangeSlider<T>, fraction: Float, orientation: Orientation = slider.orientation): Double = when (orientation) {
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