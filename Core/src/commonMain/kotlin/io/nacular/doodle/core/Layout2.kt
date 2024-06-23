package io.nacular.doodle.core

import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

/**
 * Represents an item that can be positioned.
 *
 * @see View
 */
public interface Positionable2 {
    /** The top, left, width, and height. */
    public var bounds: Rectangle

    public val preferredPosition: Point

    /** Whether this item is visible. */
    public val visible: Boolean

    /**
     * Checks whether this item contains [point].
     *
     * @param point within the item's parent
     * @return `true` IFF the point falls within the item
     */
    public operator fun contains(point: Point): Boolean

    public fun preferredSize(min: Size, max: Size): Size
}

/**
 * Layouts control the positioning of a [PositionableContainer]'s children. They are also responsible for reporting the ideal size for a view given it's contents.
 *
 * Layouts automatically take control of content positioning; therefore they should be used in preference of manually monitoring a [PositionableContainer]'s size.
 *
 * A [PositionableContainer]'s Layout will be asked to perform positioning whenever that [PositionableContainer]'s size changes or it becomes visible after one or more of its
 * children has triggered a layout.  A child will trigger a layout if its bounds change or if it changes visibility.
 *
 * @author Nicholas Eddy
 */
public interface Layout2 {
    /**
     * Called whenever the View's parent wishes to update it's size.
     *
     * @param min the smallest size this View is allowed to be
     * @param max the largest size this View is allowed to be
     * @return a value that respects [min] and [max]
     */
    public fun size(of: Sequence<Positionable2>, min: Size, max: Size): Size = max

    /**
     * Gets the child within the Positionable at the given point.  The default is to ignore these
     * calls and let the caller perform their own search for the right child.  But Layouts are
     * free to return a value here if it can be done more efficiently.
     *
     * @param of the Positionable
     * @param at The point
     * @return a result with a child, empty, or [Ignored]
     */
    public fun item(of: List<Positionable2>, at: Point): LookupResult = Ignored

    public companion object {
        /**
         * @param layout delegated to for positioning
         * @return a Layout that delegates to [layout]
         */
        public inline fun simpleLayout(crossinline layout: (items: Sequence<Positionable2>, min: Size, max: Size) -> Size): Layout2 = object: Layout2 {
            override fun size(of: Sequence<Positionable2>, min: Size, max: Size): Size = layout(of, min, max)
        }
    }
}