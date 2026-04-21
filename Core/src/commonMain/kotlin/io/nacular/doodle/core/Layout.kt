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
public abstract class Positionable internal constructor() {

    internal abstract var x_     : Double
    internal abstract var y_     : Double
    internal abstract var size_  : Size
    internal abstract var minSize: Size
    internal abstract var maxSize: Size

    /** The ideal size */
    public abstract val idealSize: Size

    /**
     * Preferred size within [min] and [max].
     *
     * @param min size allowed
     * @param max size allowed
     */
    public abstract fun preferredSize(min: Size, max: Size): Size

    /** Whether this item is visible. */
    public abstract val visible: Boolean

    /**
     * Checks whether this item contains [point].
     *
     * @param point within the item's parent
     * @return `true` IFF the point falls within the item
     */
    public abstract operator fun contains(point: Point): Boolean
/*
    /**
     * Sets item's [position] to [x] and [y]
     *
     * @param x to set
     * @param y to set
     */
    public abstract fun updatePosition(x: Double, y: Double)

    /**
     * Sets item's [Positionable.position] to the given value
     *
     * @param position to set
     */
    public fun updatePosition(position: Point): Unit = updatePosition(position.x, position.y)

    /**
     * Asks the item to choose a size within [min] and [max]
     *
     * @param min size
     * @param max size
     * @return size selected
     */
    public fun updateSize(min: Size, max: Size): Size = updateBounds(position.x, position.y, min, max)

    public fun updateSize(size: Size) { updateBounds { setWidth(size.width); setHeight(size.height) } }

    /**
     * Sets the item's [position] and asks it to choose a size within [min] and [max]
     *
     * @param x   value
     * @param y   value
     * @param min size
     * @param max size
     * @return size selected
     */
    public abstract fun updateBounds(x: Double, y: Double, min: Size, max: Size): Size

    /**
     * Sets the item's [bounds] to [rectangle]
     *
     * @param rectangle bounds
     */
    public fun updateBounds(rectangle: Rectangle) {
        updateBounds(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
    }

    /**
     * Sets the item's [bounds] to the specified values
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public fun updateBounds(x: Double, y: Double, width: Double, height: Double) {
        updateBounds {
            setPosition(x, y)
            setWidth(width)
            setHeight(height)
        }
    }

    public abstract fun updateBounds(block: BoundsUpdateContext.() -> Unit): Size*/
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
     * Positions and sizes the given [views] and returns a size appropriate to laying out these views
     * within the given constraints.
     *
     * @param views being laid out
     * @param min the smallest size to fit the views in
     * @param current size to fit the views in
     * @param max the largest size to fit the views in
     * @param insets to apply
     */
    public fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets = None): Size

    /**
     * Determines the best size to contain the [views] given the constraints. The default implementation relies on [layout]
     * for the answer.
     *
     * @param views being contained
     * @param min the smallest size to fit the views in
     * @param current size to fit the views in
     * @param max the largest size to fit the views in
     * @param insets to apply
     */
    public fun preferredSize(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets = None): Size = layout(
        views   = views,
        min     = min,
        current = current,
        max     = max,
        insets  = insets
    )

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
     * Gets the child within the Positionable at the given point. The default is to ignore these
     * calls and let the caller perform their own search for the right child. But Layouts are
     * free to return a value here if it can be done more efficiently.
     *
     * @param of the Positionable
     * @param at The point
     * @return a result with a child, empty, or [Ignored]
     */
    public fun item(of: Sequence<Positionable>, at: Point): LookupResult = Ignored

    public var Positionable.x       : Double    get() = x_;                          set(value) { x_       = value                             }
    public var Positionable.y       : Double    get() = y_;                          set(value) { y_       = value                             }
    public var Positionable.width   : Double    get() = size_.width;                 set(value) { minSize = Size(value, minSize.height); maxSize = Size(value, maxSize.height); size_ = Size(value,       size.height)    }
    public var Positionable.height  : Double    get() = size_.height;                set(value) { minSize = Size(minSize.width, value ); maxSize = Size(maxSize.width, value ); size_ = Size(size.width, value       )    }
    public var Positionable.position: Point     get() = Point    (x,        y     ); set(value) { x        = value.x; y = value.y              }
    public var Positionable.bounds  : Rectangle get() = Rectangle(position, size  ); set(value) { position = value.position; size = value.size }
    public var Positionable.size    : Size      get() = size_;                       set(value) {
        minSize = value
        maxSize = value
        size_   = value
    }

    public val Positionable.right   : Double get() = x + width;
    public val Positionable.bottom  : Double get() = y + height
    public val Positionable.center  : Point  get() = Point(x + width / 2, y + height / 2)

    public fun Positionable.assumeAnySizeWithin(min: Size, max: Size): Size = preferredSize(min, max).also {
        size    = it
        minSize = min
        maxSize = max
    }

    public fun Positionable.assume(size: Size, downTo: Size = Size.Empty, upTo: Size = Size.Infinite) {
        this.size    = size
        this.minSize = downTo
        this.maxSize = upTo
    }

    public companion object {
        /**
         * @param layout delegated to for positioning
         * @return a Layout that delegates to [layout]
         */
        public inline fun simpleLayout(crossinline block: Layout.(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets) -> Size): Layout = object: Layout {
            override fun layout(
                views  : Sequence<Positionable>,
                min    : Size,
                current: Size,
                max    : Size,
                insets : Insets
            ): Size = block(views, min, current, max, insets)
        }
    }
}