package io.nacular.doodle.core

import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.Insets.Companion.None

/**
 * Represents an item that can be positioned.
 *
 * @see View
 */
public interface Positionable {
    public val position: Point

    public val bounds: Rectangle

    public val idealSize: Size

    /** Whether this item is visible. */
    public val visible: Boolean

    /**
     * Checks whether this item contains [point].
     *
     * @param point within the item's parent
     * @return `true` IFF the point falls within the item
     */
    public operator fun contains(point: Point): Boolean

    public fun updateBounds(x: Double, y: Double, min: Size, max: Size): Size

    public fun updatePosition(x: Double, y: Double)

    public fun updateBounds(rectangle: Rectangle) {
        updateBounds(rectangle.x, rectangle.y, rectangle.size, rectangle.size)
    }
}

/**
 * The result of [Layout.item].
 */
public sealed class LookupResult {
    /** Indicates the Layout ignores the call */
    public object Ignored: LookupResult()

    /** Indicates that nothing was found */
    public object Empty: LookupResult()

    /**
     * The item that was found
     *
     * @property item that was found
     */
    public class Found(public val item: Positionable): LookupResult()
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
public interface Layout {

    /**
     * Called whenever the View's parent wishes to update it's size.
     *
     * @param views being laid out
     * @param min the smallest size to fit the views in
     * @param current size to fit the views in
     * @param max the largest size to fit the views in
     * @param insets to apply
     * @return a value that respects [min] and [max]
     */
    public fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets = None): Size = max

    /**
     * Gets the child within the Positionable at the given point.  The default is to ignore these
     * calls and let the caller perform their own search for the right child.  But Layouts are
     * free to return a value here if it can be done more efficiently.
     *
     * @param of the Positionable
     * @param at The point
     * @return a result with a child, empty, or [Ignored]
     */
    public fun item(of: Sequence<Positionable>, at: Point): LookupResult = Ignored

    public companion object {
        /**
         * @param layout delegated to for positioning
         * @return a Layout that delegates to [layout]
         */
        public inline fun simpleLayout(crossinline layout: (items: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets) -> Size): Layout = object: Layout {
            override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size = layout(views, min, current, max, insets)
        }
    }
}