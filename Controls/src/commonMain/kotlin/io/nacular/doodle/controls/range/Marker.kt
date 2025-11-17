package io.nacular.doodle.controls.range

import io.nacular.doodle.utils.Interpolator
import kotlin.math.max
import kotlin.math.round

/**
 * Determines where marks are placed on a [ValueSlider]. These marks are used to indicate
 * specific values or provide snapping behavior.
 */
public interface Marker<T: Comparable<T>> {
    /**
     * A sequence of values that should be marked on a slider.
     *
     * @param range of the slider being marked
     * @param interpolator of the slider being marked
     * @return a sequence of marks values in the range [0,1]
     */
    public fun marks(range: ClosedRange<T>, interpolator: Interpolator<T>): Sequence<Float>

    /**
     * Returns the closest marked value to [progress] based on the sequence that [marks] would return for the same [range]
     * and [interpolator].
     *
     * @param range of the slider being marked
     * @param interpolator of the slider being marked
     * @param progress to check
     * @return the mark closest [to] this value, or `null` if [marks] is empty.
     */
    public fun nearest(range: ClosedRange<T>, interpolator: Interpolator<T>, progress: Float): Float?
}

/**
 * Returns a [Marker] that will create evenly spaced marks that start at `0` and end at `1`.
 *
 * @param intermediateMarks indicates the number of marks between the first and last marker
 */
public fun <T: Comparable<T>> evenMarker(intermediateMarks: Int): Marker<T> = object: Marker<T> {
    private val increment = 1f / (max(0, intermediateMarks) + 1)

    override fun marks(range: ClosedRange<T>, interpolator: Interpolator<T>) = generateSequence(0f) { current ->
        current.takeIf { it < 1f }?.let { it + increment }
    }

    override fun nearest(range: ClosedRange<T>, interpolator: Interpolator<T>, progress: Float) = (round(progress / increment) * increment).coerceIn(0f..1f)
}