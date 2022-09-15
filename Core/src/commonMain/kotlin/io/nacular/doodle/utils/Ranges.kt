package io.nacular.doodle.utils

/**
 * @param other range to test with
 * @return a range representing the intersection of this one with [other]. The result may be an empty range if there is no overlap
 */
public infix fun <T: Comparable<T>> ClosedRange<T>.intersect(other: ClosedRange<T>): ClosedRange<T> = when {
    other.endInclusive < start        -> start                     .. start
    other.start        > endInclusive -> endInclusive              .. endInclusive
    else                              -> maxOf(start, other.start) .. minOf(endInclusive, other.endInclusive)
}

/**
 * @param other range to test with
 * @return `true` IFF this range intersects with [other]
 */
public infix fun <T: Comparable<T>> ClosedRange<T>.intersects(other: ClosedRange<T>): Boolean = !(start > other.endInclusive || endInclusive < other.start)

/** The span of a range: `endInclusive - start`*/
public val ClosedRange<Int>.size: Int get() = endInclusive - start

/** The span of a range: `endInclusive - start`*/
public val ClosedRange<Double>.size: Double get() = endInclusive - start

/** The span of a range: `endInclusive - start`*/
public val ClosedRange<Float>.size: Float get() = endInclusive - start