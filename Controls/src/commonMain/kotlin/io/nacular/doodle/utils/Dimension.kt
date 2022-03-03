package io.nacular.doodle.utils

import io.nacular.doodle.core.View
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public enum class Dimension { Width, Height }

/**
 * Utility to help speed up working with sets of [Dimension].
 */
internal class DimensionSet(private val delegate: Set<Dimension>): Set<Dimension> by delegate {
    private val hasWidth  = Dimension.Width in delegate
    private val hasHeight = Dimension.Height in delegate

    override fun contains(element: Dimension): Boolean = when (element) {
        Dimension.Width  -> hasWidth
        Dimension.Height -> hasHeight
    }
}

internal inline fun dimensionSetProperty(initial: Set<Dimension>, noinline onChange: View.(old: Set<Dimension>, new: Set<Dimension>) -> Unit = { _,_ -> }) = object: ReadWriteProperty<View, Set<Dimension>> {
    private var value = DimensionSet(initial)

    override fun getValue(thisRef: View, property: KProperty<*>) = value

    override fun setValue(thisRef: View, property: KProperty<*>, value: Set<Dimension>) {
        if (this.value == value) return

        val old = this.value
        this.value = DimensionSet(value)

        onChange(thisRef, old, this.value)
    }
}
