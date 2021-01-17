package io.nacular.doodle.core

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
interface Positionable {
    var x          : Double
    var y          : Double
    var size       : Size
    var width      : Double
    var height     : Double
    var bounds     : Rectangle
    val visible    : Boolean
    var position   : Point
    val idealSize  : Size?
    val minimumSize: Size

    operator fun contains(point: Point): Boolean
}

/**
 * Represents an item whose children ([Positionable]s) are being manipulated by a [Layout].
 *
 * @see Container
 */
interface PositionableContainer {
    val size       : Size
    val width      : Double
    val height     : Double
    val insets     : Insets
    val children   : List<Positionable>
    var idealSize  : Size?
    var minimumSize: Size
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
sealed class LookupResult {
    /** Indicates the Layout ignores the call */
    object Ignored: LookupResult()

    /** Indicates that nothing was found */
    object Empty: LookupResult()

    /** The item that was found */
    class Found(val child: Positionable): LookupResult()
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
interface Layout {
    /**
     * Lays out the children of the given [Positionable].
     *
     * @param container to be laid out
     */
    fun layout(container: PositionableContainer)

    /**
     * Returns the minimum size of the Positionable based on its contents.
     *
     * @param  container The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the minimum size
     */
    fun minimumSize(container: PositionableContainer, default: Size = Empty): Size = default

    /**
     * Returns the ideal size of the Positionable based on its contents.
     *
     * @param  container The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the ideal size
     */
    fun idealSize(container: PositionableContainer, default: Size? = null): Size? = default

    /**
     * Gets the child within the Positionable at the given point.  The default is to ignore these
     * calls and let the caller perform their own search for the right child.  But Layouts are
     * free to return a value here if it can be done more efficiently.
     *
     * @param of the Positionable
     * @param at The point
     * @return a result with a child, empty, or [Ignored]
     */
    fun child(of: PositionableContainer, at: Point): LookupResult = Ignored
}

inline fun simpleLayout(crossinline layout: (container: PositionableContainer) -> Unit) = object: Layout {
    override fun layout(container: PositionableContainer) = layout(container)
}

infix fun Layout.then(onLayout: (PositionableContainer) -> Unit) = simpleLayout { container ->
    layout(container)

    onLayout(container)
}
