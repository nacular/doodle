package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Insets

/**
 * Represents a View that can have children and [Layout].
 */
interface Container: Iterable<View> {
    /** Hint to [layout] that children within the Container should be inset from the edges */
    var insets: Insets

    /**
     * Manages the positioning of children within the Container.  The framework will call use this
     * to reposition the children whenever the Container's bounds change, or the bounds, visibility
     * of a child changes.
     */
    var layout: Layout?

    var isFocusCycleRoot: Boolean

    /** The list of children within the Container */
    val children: MutableList<View>

    /**
     * @param view in question
     * @return `true` IFF [view] is a descendant of this Container
     */
    infix fun ancestorOf(view: View): Boolean

    /**
     * @param at the x,y within the Container's coordinate-space
     * @return a View if one is found to contain the given point
     */
    fun child(at: Point): View?

    override fun iterator() = children.iterator()
}