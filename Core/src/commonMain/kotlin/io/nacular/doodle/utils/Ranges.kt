package io.nacular.doodle.utils

/**
 * Created by Nicholas Eddy on 2/12/18.
 */

public infix fun <T: Comparable<T>> ClosedRange<T>.intersect (other: ClosedRange<T>): ClosedRange<T> = maxOf(start, other.start) .. minOf(endInclusive, other.endInclusive)
public infix fun <T: Comparable<T>> ClosedRange<T>.intersects(other: ClosedRange<T>): Boolean        = start in other || endInclusive in other

public val ClosedRange<Int   >.size: Int    get() = endInclusive - start
public val ClosedRange<Double>.size: Double get() = endInclusive - start
public val ClosedRange<Float >.size: Float  get() = endInclusive - start