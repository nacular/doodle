package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 2/12/18.
 */

infix fun <T: Comparable<T>> ClosedRange<T>.intersect (other: ClosedRange<T>) = maxOf(start, other.start) .. minOf(endInclusive, other.endInclusive)
infix fun <T: Comparable<T>> ClosedRange<T>.intersects(other: ClosedRange<T>) = start in other || endInclusive in other

val ClosedRange<Int   >.size get() = endInclusive - start
val ClosedRange<Double>.size get() = endInclusive - start
val ClosedRange<Float >.size get() = endInclusive - start