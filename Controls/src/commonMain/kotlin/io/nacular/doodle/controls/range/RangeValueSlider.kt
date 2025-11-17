package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.ConfinedRangeModel
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowLeft
import io.nacular.doodle.event.KeyText.Companion.ArrowRight
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import io.nacular.doodle.utils.Interpolator
import io.nacular.doodle.utils.observable

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
    internal val interpolator: Interpolator<T>,
    private  val function    : InvertibleFunction = LinearFunction,
): View() where T: Comparable<T> {
    /**
     * Number of ticks the slider should have.
     */
    @Deprecated(message = "Use marker instead", replaceWith = ReplaceWith("marker"))
    public var ticks: Int; get() = marker?.marks(model.limits, interpolator)?.toList()?.size ?: 0; set(new) {
        marker = when {
            new < 1 -> null
            else    -> evenMarker(new - 2)
        }
    }

    /**
     * Provides marks that should show up on the slider.
     */
    public var marker: Marker<T>? by observable(null) { _,_ ->
        ticksChanged ()
        markerChanged()

        refreshSnapping()
    }

    /**
     * Determines how the slider snaps to marks.
     */
    public var snappingPolicy: SnappingPolicy<T>? by observable(null) { _,_ -> refreshSnapping() }

    /**
     * Indicates whether the slider should only take on values at the specify tick interval.
     *
     * @see ticks
     */
    @Deprecated(message = "Use snappingPolicy", replaceWith = ReplaceWith("snappingPolicy"))
    public var snapToTicks: Boolean by observable(snappingPolicy != null) { _,new ->
        snappingPolicy = when {
            new  -> alwaysSnap()
            else -> null
        }
    }

    @Deprecated("Will go away, do not use")
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
        val m = marker
        val s = snappingPolicy

        val start = when {
            m != null && s != null -> m.nearest(model.limits, interpolator, new.start)?.takeIf { s.shouldSnapTo(it.value, from = new.start.value) } ?: new.start
            else                    -> new.start
        }.value

        val end = when {
            m != null && s != null -> m.nearest(model.limits, interpolator, new.endInclusive)?.takeIf { s.shouldSnapTo(it.value, from = new.endInclusive.value) } ?: new.endInclusive
            else                    -> new.endInclusive
        }.value

        model.range = start .. end
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
    @Deprecated("Use markerChanged instead", ReplaceWith("markerChanged"))
    protected abstract fun ticksChanged()

    protected open fun markerChanged() { ticksChanged() } // TODO: make abstract

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

    private val Float.value get() = interpolator.lerp(range.start, range.endInclusive, function(this))

    internal fun valueToFraction(value: T) = function.inverse(interpolator.progress(range.start, range.endInclusive, value))

    /**
     * Update [value] to ensure snapped to the closest tick
     */
    private fun refreshSnapping() { value = value }
}

/**
 * The sequence of marks for a [RangeValueSlider] or `emptySequence()` if it has none.
 */
public val <T: Comparable<T>> RangeValueSlider<T>.marks: Sequence<Float> get() = marker?.marks(model.limits, interpolator)?.filter { it in 0f..1f } ?: emptySequence()


/**
 * Sets the RangeValueSlider's marker based on the given values.
 *
 * @param using these values to set each mark
 */
public fun <T: Comparable<T>> RangeValueSlider<T>.mark(vararg using: T) = mark(using.toList())

/**
 * Sets the RangeValueSlider's marker based on the given Iterable.
 *
 * @param using this iterable to set each mark
 */
public fun <T: Comparable<T>> RangeValueSlider<T>.mark(using: Iterable<T>) {
    marker = object: Marker<T> {
        val items = using.filter { it in model.limits }.map { valueToFraction(it) }.sorted()

        override fun marks(range: ClosedRange<T>, interpolator: Interpolator<T>) = items.asSequence()

        override fun nearest(range: ClosedRange<T>, interpolator: Interpolator<T>, progress: Float): Float {
            val index       = items.binarySearch(progress)
            val insertIndex = -(index + 1)

            return when {
                index >= 0                    -> items[index]
                insertIndex == 0              -> items.firstOrNull() ?: 0f
                insertIndex > items.lastIndex -> items.lastOrNull () ?: 0f
                else -> {
                    val previous = items[insertIndex - 1]
                    val next     = items[insertIndex    ]

                    if (progress - previous < next - progress) previous else next
                }
            }
        }
    }
}