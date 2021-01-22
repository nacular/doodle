package io.nacular.doodle.core

import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.layout.Insets

/**
 * Represents an item within a [PositionableContainer] that a [Layout] can position.
 *
 * @see View
 */
public interface Positionable {
    public var x          : Double
    public var y          : Double
    public var size       : Size
    public var width      : Double
    public var height     : Double
    public var bounds     : Rectangle
    public val visible    : Boolean
    public var position   : Point
    public val idealSize  : Size?
    public val minimumSize: Size

    public operator fun contains(point: Point): Boolean
}

/**
 * Represents an item whose children ([Positionable]s) are being manipulated by a [Layout].
 *
 * @see Container
 */
public interface PositionableContainer {
    public val size       : Size
    public val width      : Double
    public val height     : Double
    public val insets     : Insets
    public val children   : List<Positionable>
    public var idealSize  : Size?
    public var minimumSize: Size
}

internal class PositionableContainerWrapper(val view: View): PositionableContainer {
    override val size        get() = view.size
    override val width       get() = view.width
    override val height      get() = view.height
    override var idealSize   get() = view.idealSize;   set(value) { view.idealSize   = value }
    override var minimumSize get() = view.minimumSize; set(value) { view.minimumSize = value }
    override val insets      get() = view.insets_
    override val children    get() = view.children_

    override fun equals(other: Any?): Boolean = when (other) {
        is View                         -> view == other
        is PositionableContainerWrapper -> view == other.view
        else                            -> false
    }

    override fun hashCode(): Int = view.hashCode()
}

/**
 * The result of [Layout.child].
 */
public sealed class LookupResult {
    /** Indicates the Layout ignores the call */
    public object Ignored: LookupResult()

    /** Indicates that nothing was found */
    public object Empty: LookupResult()

    /** The item that was found */
    public class Found(public val child: Positionable): LookupResult()
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
     * Lays out the children of the given [Positionable].
     *
     * @param container to be laid out
     */
    public fun layout(container: PositionableContainer)

    /**
     * Returns the minimum size of the Positionable based on its contents.
     *
     * @param  container The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the minimum size
     */
    public fun minimumSize(container: PositionableContainer, default: Size = Empty): Size = default

    /**
     * Returns the ideal size of the Positionable based on its contents.
     *
     * @param  container The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the ideal size
     */
    public fun idealSize(container: PositionableContainer, default: Size? = null): Size? = default

    /**
     * Gets the child within the Positionable at the given point.  The default is to ignore these
     * calls and let the caller perform their own search for the right child.  But Layouts are
     * free to return a value here if it can be done more efficiently.
     *
     * @param of the Positionable
     * @param at The point
     * @return a result with a child, empty, or [Ignored]
     */
    public fun child(of: PositionableContainer, at: Point): LookupResult = Ignored

    public companion object {
        /**
         * @param layout delegated to for positioning
         * @return a Layout that delegates to [layout]
         */
        public inline fun simpleLayout(crossinline layout: (container: PositionableContainer) -> Unit): Layout = object: Layout {
            override fun layout(container: PositionableContainer) = layout(container)
        }
    }
}

/**
 * Provides a way to get notified each time a [Layout] is done positioning.
 *
 * ```kotlin
 *
 * val layout = MyLayout then { justLayedOutContainer ->
 *     // do something
 * }
 *
 * ```
 *
 * @param onLayout is called after laying out a container
 * @return a Layout that performs the positioning of the original and then calls [onLayout]
 */
public infix fun Layout.then(onLayout: (PositionableContainer) -> Unit): Layout = simpleLayout { container ->
    layout(container)

    onLayout(container)
}
