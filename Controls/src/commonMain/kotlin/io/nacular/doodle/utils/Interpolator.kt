package io.nacular.doodle.utils

import io.nacular.measured.units.Measure
import io.nacular.measured.units.Units
import io.nacular.measured.units.div

/**
 * Allows interpolating between two start and end values of [T] given some progress.
 */
public interface Interpolator<T> {
    /**
     * Interpolates between [start] and [end] (inclusive).
     *
     * @param start value
     * @param end value
     * @param progress between [start] and [end] in the range [0-1]
     * @return the value
     */
    public fun lerp(start: T, end: T, progress: Float): T

    /**
     * Returns the progress of [value] between [start] and [end] (inclusive).
     *
     * @param start value
     * @param end value
     * @param value being checked
     * @return the progress of [value] between [start] and [end]
     */
    public fun progress(start: T, end: T, value: T): Float

    /**
     * Returns a number to represent [value] to accessibility systems.
     *
     * @param start value
     * @param end value
     * @param value being checked
     * @return the numeric representation of [value]
     */
    public fun accessibleNumericValue(start: T, end: T, value: T): Double = progress(start, end, value).toDouble()
}

/**
 * [Interpolator] for [Measure]s
 */
public val <T: Units> T.interpolator: Interpolator<Measure<T>> get() = object: Interpolator<Measure<T>> {
    override fun lerp    (start: Measure<T>, end: Measure<T>, progress: Float     ) = io.nacular.doodle.utils.lerp(start, end, progress)
    override fun progress(start: Measure<T>, end: Measure<T>, value   : Measure<T>) = ((value - start) / (end - start)).toFloat()

    override fun accessibleNumericValue(start: Measure<T>, end: Measure<T>, value: Measure<T>) = value.amount
}

/**
 * [Interpolator] for [Char].
 */
internal object CharInterpolator: Interpolator<Char> {
    override fun lerp    (start: Char, end: Char, progress: Float) = (lerp(start.code.toDouble(), end.code.toDouble(), progress)).toInt().toChar()
    override fun progress(start: Char, end: Char, value   : Char) = ((value - start).toDouble() / (end - start)).toFloat()

    override fun accessibleNumericValue(start: Char, end: Char, value: Char): Double = value.code.toDouble()
}