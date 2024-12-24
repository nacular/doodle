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
    /** The current screen location */
    public val position: Point

    /** The current screen location/size */
    public val bounds: Rectangle

    /** The current preferred size */
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

    /**
     * Sets item's [position] to [x] and [y]
     *
     * @param x to set
     * @param y to set
     */
    public fun updatePosition(x: Double, y: Double)

    /**
     * Sets item's [Positionable.position] to the given value
     *
     * @param position to set
     */
    public fun updatePosition(position: Point) { updatePosition(position.x, position.y) }

    /**
     * Asks the item to choose a size within [min] and [max]
     *
     * @param min size
     * @param max size
     * @return size selected
     */
    public fun updateSize(min: Size, max: Size): Size = updateBounds(position.x, position.y, min, max)

    public fun updateSize(size: Size) { updateBounds(Rectangle(position, size)) }

    /**
     * Sets the item's [position] and asks it to choose a size within [min] and [max]
     *
     * @param x   value
     * @param y   value
     * @param min size
     * @param max size
     * @return size selected
     */
    public fun updateBounds(x: Double, y: Double, min: Size, max: Size): Size

    /**
     * Sets the item's [bounds] to [rectangle]
     *
     * @param rectangle size
     */
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
 * Layouts control the positioning of a sequence of [View]s within a [Size]. They are also responsible for reporting the ideal size for a view given its contents.
 *
 * Layouts automatically take control of content positioning; therefore they should be used in preference of manually monitoring a [View]'s size.
 *
 * A [View]'s Layout will be asked to perform positioning whenever its size changes, or it becomes visible after one or more of its
 * children has triggered a layout. A child will trigger a layout if its bounds change or if it changes visibility.
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
     * Indicates whether a layout is needed because of the given size change to a container.
     * This is called whenever the container's `size` changes.
     *
     * @param old size of the container
     * @param new size of the container
     * @return `true` if a layout is needed
     */
    public fun requiresLayout(old: Size, new: Size): Boolean = true

    /**
     * Indicates whether a layout is needed because of the given bounds change to a child within a container.
     * This is called whenever the child's `size` changes.
     *
     * @param child whose bounds has changed
     * @param within the given size
     * @param old bounds of the child
     * @param new bounds of the child
     * @return `true` if a layout is needed
     */
    public fun requiresLayout(child: Positionable, within: Size, old: Rectangle, new: Rectangle): Boolean = true

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