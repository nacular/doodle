package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.layout.Insets


interface Positionable {
    var x          : Double
    var y          : Double
    var size       : Size
    var width      : Double
    var height     : Double
    var bounds     : Rectangle
    val parent     : PositionableContainer?
    val visible    : Boolean
    var position   : Point
    var idealSize  : Size?
    var minimumSize: Size

    operator fun contains(point: Point): Boolean
}

interface PositionableContainer {
    var size       : Size
    var width      : Double
    var height     : Double
    val insets     : Insets
    val layout     : Layout?
    val parent     : PositionableContainer?
    val children   : List<Positionable>
    var idealSize  : Size?
    var minimumSize: Size
}

open class PositionableWrapper(val view: View): Positionable {
    override var x           get() = view.x;           set(value) { view.x           = value }
    override var y           get() = view.y;           set(value) { view.y           = value }
    override var size        get() = view.size;        set(value) { view.size        = value }
    override var width       get() = view.width;       set(value) { view.width       = value }
    override var height      get() = view.height;      set(value) { view.height      = value }
    override var bounds      get() = view.bounds;      set(value) { view.bounds      = value }
    override val parent      get() = view.parent?.let { PositionableContainerWrapper(it) }
    override val visible     get() = view.visible
    override var position    get() = view.position;    set(value) { view.position    = value }
    override var idealSize   get() = view.idealSize;   set(value) { view.idealSize   = value }
    override var minimumSize get() = view.minimumSize; set(value) { view.minimumSize = value }

    override fun hashCode() = view.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PositionableContainerWrapper) return false
        return view == other.view
    }

    override operator fun contains(point: Point) = point in view
}

class PositionableContainerWrapper(view: View): PositionableWrapper(view), PositionableContainer {
    override val insets      get() = view.insets_
    override val layout      get() = view.layout_
    override val children    get() = view.children_.map { PositionableWrapper(it) }
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
abstract class Layout {
    /**
     * Lays out the children of the given [Positionable].
     *
     * @param container to be laid out
     */
    abstract fun layout(container: PositionableContainer)

    /**
     * Returns the minimum size of the Positionable based on its contents.
     *
     * @param  container The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the minimum size
     */
    open fun minimumSize(container: PositionableContainer, default: Size = Empty): Size = default

    /**
     * Returns the ideal size of the Positionable based on its contents.
     *
     * @param  container The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the ideal size
     */
    open fun idealSize(container: PositionableContainer, default: Size? = null): Size? = default

    /**
     * Gets the child within the Positionable at the given point.
     *
     * @param of the Positionable
     * @param at The point
     * @return The child (null if no child contains the given point)
     */
    open fun child(of: PositionableContainer, at: Point): Positionable? = null
}