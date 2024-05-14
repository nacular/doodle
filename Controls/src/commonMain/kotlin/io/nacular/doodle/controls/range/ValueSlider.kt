package io.nacular.doodle.controls.range

import io.nacular.doodle.accessibility.SliderRole
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.bind
import io.nacular.doodle.controls.binding
import io.nacular.doodle.controls.numberTypeConverter
import io.nacular.doodle.core.ContentDirection.LeftRight
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
 * Base class for controls that represent sliders. These controls have a single [value] that is bound between a [range]
 * defined by [model]. ValueSliders can be conceptualized as mapping between two domains as follows:
 *
 * @param model used to represent the slider's value and limits
 * @param interpolator used in mapping between [T] and the slider's domain: [0-1]
 * @param function used to map between the slider's input and output.
 */
public abstract class ValueSlider<T> internal constructor(
                  model       : ConfinedValueModel<T>,
    private   val interpolator: Interpolator<T>,
    private   val function    : InvertibleFunction = LinearFunction,
    protected val role        : SliderRole         = SliderRole()
): View(role) where T: Comparable<T> {

    @Deprecated("This can only be used with Number types. Use version that takes converter instead")
    protected constructor(model: ConfinedValueModel<T>, type: KClass<T>, function: InvertibleFunction = LinearFunction): this(model, numberTypeConverter(type), function)

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
    public var model: ConfinedValueModel<T> = model; set(new) {
        field.valueChanged -= modelChanged

        field = new.also {
            it.valueChanged += modelChanged
            roleBinding = role.bind(it, interpolator, valueAccessibilityLabeler)
        }
    }

    /**
     * Slider's current value.
     */
    public var value: T get() = model.value; set(new) {
        fraction = valueToFraction(new)
    }

    /**
     * Slider's current range: start - end.
     */
    public var range: ClosedRange<T> get() = model.limits; set(new) { model.limits = new }

    /** Human-understandable text to represent the current value if the number is insufficient. */
    public var valueAccessibilityLabeler: ((T) -> String)? by observable(null) { _,new ->
        roleBinding = role.bind(model, interpolator, new)
    }

    internal var fraction: Float get() = valueToFraction(value); set(new) {
        val frac = when (val s = snapSize?.toFloat()) {
            null -> new
            else -> round(new / s) * s
        }.coerceIn(0f..1f)

        model.value = interpolator.lerp(range.start, range.endInclusive, function(frac))
    }

    private var roleBinding by binding(role.bind(model, interpolator, valueAccessibilityLabeler))

    /** Notifies of changes to [value] */
    protected abstract fun changed(old: T, new: T)

    /** Notifies of changes to [range] */
    protected abstract fun limitsChanged(old: ClosedRange<T>, new: ClosedRange<T>)

    /** Notifies of changes to [ticks] */
    protected abstract fun ticksChanged()

    private val modelChanged: (ConfinedValueModel<T>, T, T) -> Unit = { _,old,new ->
        changed(old, new)
    }

    private val limitsChanged: (ConfinedValueModel<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { _,old,new ->
        limitsChanged(old, new)
    }

    init {
        model.valueChanged  += modelChanged
        model.limitsChanged += limitsChanged
    }

    internal fun handleKeyPress(event: KeyEvent) {
        val (incrementKey, decrementKey) = when (contentDirection) {
            LeftRight -> ArrowRight to ArrowLeft
            else      -> ArrowLeft  to ArrowRight
        }

        when (event.key) {
            ArrowUp,   incrementKey -> fraction += 0.1f
            ArrowDown, decrementKey -> fraction -= 0.1f
        }
    }

    private fun valueToFraction(value: T) = function.inverse(interpolator.progress(range.start, range.endInclusive, value))
}