package io.nacular.doodle.controls

import io.nacular.doodle.utils.observable
import kotlin.properties.ReadWriteProperty

/**
 * Represents a relationship between items that can be dissolved.
 */
public interface Binding {
    /** Dissolves the relationship */
    public fun unbind()
}

/**
 * Tracks a [Binding] and unbinds it when a new one replaces it.
 */
public fun binding(initial: Binding): ReadWriteProperty<Any, Binding> = observable(initial) { old,_ ->
    old.unbind()
}