package io.nacular.doodle.controls.range

import io.nacular.measured.units.Measure
import io.nacular.measured.units.Units
import io.nacular.measured.units.abs
import kotlin.math.abs

/**
 * Determines the snapping behavior for a [ValueSlider].
 */
public interface SnappingPolicy<T> {
    /**
     * @return `true` if the slider should snap to [value] when at [from].
     * @param value of snap point
     * @param from the slider's current value
     */
    public fun shouldSnapTo(value: T, from: T): Boolean
}

/**
 * Creates a [SnappingPolicy] from the given lambda.
 *
 * @param block that is run whenever [SnappingPolicy.shouldSnapTo] is called
 */
public fun <T> snap(block: (value: T, from: T) -> Boolean): SnappingPolicy<T> = object: SnappingPolicy<T> {
    override fun shouldSnapTo(value: T, from: T) = block(value, from)
}

/**
 * @return a [SnappingPolicy] that always snaps to the value.
 */
public fun <T> alwaysSnap(): SnappingPolicy<T> = object: SnappingPolicy<T> {
    override fun shouldSnapTo(value: T, from: T) = true
}

/**
 * @return a [SnappingPolicy] that snaps when the slider is within a given [distance] to a value.
 */
public fun <T: Number> snapWithin(distance: T): SnappingPolicy<T> = object: SnappingPolicy<T> {
    override fun shouldSnapTo(value: T, from: T) = abs(value.toDouble() - from.toDouble()) <= distance.toDouble()
}

/**
 * @return a [SnappingPolicy] that snaps when the slider is within a given [distance] to a value.
 */
public fun <T: Units> snapWithin(distance: Measure<T>): SnappingPolicy<Measure<T>> = object: SnappingPolicy<Measure<T>> {
    override fun shouldSnapTo(value: Measure<T>, from: Measure<T>) = abs(value - from) <= distance
}