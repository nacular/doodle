package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.ConfinedRangeModel
import io.nacular.doodle.controls.numberTypeConverter
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowLeft
import io.nacular.doodle.event.KeyText.Companion.ArrowRight
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import io.nacular.doodle.utils.Interpolator
import io.nacular.doodle.utils.observable
import kotlin.math.max
import kotlin.math.round
import kotlin.reflect.KClass

/**
 * Base class for controls that represent range sliders. These controls have a [value] range that is bound between a [range]
 * defined by [model]. ValueSliders can be conceptualized as mapping between two domains as follows:
 *
 * @param model used to represent the slider's value and limits
 * @param interpolator used in mapping between [T] and the slider's domain: [0-1]
 * @param function used to map between the slider's input and output.
 */
public abstract class RangeValueSlider<T> internal constructor(
                model       : ConfinedRangeModel<T>,
    private val interpolator: Interpolator<T>,
    private val function    : InvertibleFunction = LinearFunction,
): View() where T: Comparable<T> {

    @Deprecated("Use version that takes converter instead")
    protected constructor(model: ConfinedRangeModel<T>, type: KClass<T>, function: InvertibleFunction = LinearFunction): this(model, numberTypeConverter(type), function)

    /**
     * Indicates whether the slider should only take on values at the specify tick interval.
     *
     * @see ticks
     */
    public var snapToTicks: Boolean by observable(false) { _,new ->
        if (new) {
            value = value // update value to ensure snapped to the closest tick
        }

        ticksChanged()
    }

    /**
     * Number of ticks the slider should have.
     */
    public var ticks: Int = 0; set(new) {
        field = max(0, new)

        snapSize = if (field > 1) 1.0 / (field - 1) else null
    }

    // FIXME: Make private
    public var snapSize: Double? = null; private set(new) {
        if (new == field) return

        field = new

        if (snapToTicks) {
            value = value // update value to ensure snapped to the closest tick
        }

        ticksChanged()
    }

    /**
     * Model that represents the slider's [value] and [range].
     */
    public var model: ConfinedRangeModel<T> = model; set(new) {
        field.rangeChanged -= modelChanged

        field = new.also {
            it.rangeChanged += modelChanged
        }
    }

    /**
     * Slider's current value.
     */
    public var value: ClosedRange<T> get() = model.range; set(new) {
        fraction = valueToFraction(new.start) .. valueToFraction(new.endInclusive)
    }

    /**
     * Slider's current range: start - end.
     */
    public var range: ClosedRange<T> get() = model.limits; set(new) { model.limits = new }

    /**
     * Value of the slider as a fraction range between [0-1].
     */
    public var fraction: ClosedRange<Float> get() = valueToFraction(value.start)..valueToFraction(value.endInclusive); internal set(new) {
        val s = snapSize?.toFloat()

        val start = when {
            s == null || !snapToTicks -> new.start
            else                      -> round(new.start / s) * s
        }.coerceIn(0f..1f)

        val end = when {
            s == null || !snapToTicks -> new.endInclusive
            else                      -> round(new.endInclusive / s) * s
        }.coerceIn(0f..1f)

        model.range = interpolator.lerp(range.start, range.endInclusive, function(start)) ..
                interpolator.lerp(range.start, range.endInclusive, function(end))
    }

    public fun increment(percent: Float = 0.1f) { fraction = fraction.start + percent .. fraction.endInclusive + percent }
    public fun decrement(percent: Float = 0.1f) { fraction = fraction.start - percent .. fraction.endInclusive - percent }

    public fun incrementStart(percent: Float = 0.1f) { fraction = fraction.start + percent .. fraction.endInclusive }
    public fun incrementEnd  (percent: Float = 0.1f) { fraction = fraction.start .. fraction.endInclusive + percent }

    public fun decrementStart(percent: Float = 0.1f) { fraction = fraction.start - percent .. fraction.endInclusive }
    public fun decrementEnd  (percent: Float = 0.1f) { fraction = fraction.start .. fraction.endInclusive - percent }

    /** Notifies of changes to [value] */
    protected abstract fun changed(old: ClosedRange<T>, new: ClosedRange<T>)

    /** Notifies of changes to [range] */
    protected abstract fun limitsChanged(old: ClosedRange<T>, new: ClosedRange<T>)

    /** Notifies of changes to [ticks] */
    protected abstract fun ticksChanged()

    private val modelChanged: (ConfinedRangeModel<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { _,old,new ->
        changed(old, new)
    }

    private val limitsChanged: (ConfinedRangeModel<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { _,old,new ->
        limitsChanged(old, new)
    }

    init {
        model.rangeChanged  += modelChanged
        model.limitsChanged += limitsChanged
    }

    internal fun handleKeyPress(event: KeyEvent) {
        val (incrementKey, decrementKey) = when (contentDirection) {
            ContentDirection.LeftRight -> ArrowRight to ArrowLeft
            else                       -> ArrowLeft  to ArrowRight
        }

        when (event.key) {
            ArrowUp,   incrementKey -> increment()
            ArrowDown, decrementKey -> decrement()
        }
    }

    private fun valueToFraction(value: T) = function.inverse(interpolator.progress(range.start, range.endInclusive, value))
}