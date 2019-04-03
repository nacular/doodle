package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.layout.Insets


interface Positionable {
    var size       : Size
    val width      : Double
    val height     : Double
    val insets     : Insets
    val parent     : View?      // TODO: Should this be a Positionable?
    val children   : List<View> // TODO: Should this be a List<Positionable>?
    var idealSize  : Size?
    var minimumSize: Size
}

class PositionableWrapper(val view: View): Positionable {
    override var size        get() = view.size
        set(value) { view.size = value }
    override val width       get() = view.width
    override val height      get() = view.height
    override val insets      get() = view.insets_
    override val parent      get() = view.parent
    override val children    get() = view.children_
    override var idealSize   get() = view.idealSize
        set(value) { view.idealSize = value }
    override var minimumSize get() = view.minimumSize
        set(value) { view.minimumSize = value }
}

/**
 * Layouts control the positioning of a [View]'s children. They are also responsible for reporting the ideal size for a view given it's contents.
 *
 * Layouts automatically take control of content positioning; therefore they should be used in preference of manually monitoring a [View]'s size.
 *
 * A [View]'s Layout will be asked to perform positioning whenever that [View]'s size changes or it becomes visible after one or more of its
 * children has triggered a layout.  A child will trigger a layout if its bounds change or if it changes visibility.
 *
 * @author Nicholas Eddy
 */
abstract class Layout {
    /**
     * Lays out the children of the given [Positionable].
     *
     * @param positionable to be laid out
     */
    abstract fun layout(positionable: Positionable)

    /**
     * Returns the minimum size of the Positionable based on its contents.
     *
     * @param  positionable The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the minimum size
     */
    open fun minimumSize(positionable: Positionable, default: Size = Empty): Size = default

    /**
     * Returns the ideal size of the Positionable based on its contents.
     *
     * @param  positionable The Positionable being investigated
     * @param  default The size to use if one can't be calculated
     * @return the ideal size
     */
    open fun idealSize(positionable: Positionable, default: Size? = null): Size? = default

    /**
     * Gets the child within the Positionable at the given point.
     *
     * @param of the Positionable
     * @param at The point
     * @return The child (null if no child contains the given point)
     */
    open fun child(of: Positionable, at: Point): View? = null

    /**
     * Causes the Layout to position the children of the given [View]. NOTE: stateful layouts will only
     * position the items they track, while stateless Layouts will iterate over the [View]'s children.
     *
     * @param view The View to lay out
     */
    internal fun layout(view: View) = layout(PositionableWrapper(view))

    /**
     * Returns the minimum size of the [View] based on its contents.
     *
     * @param  view The View being investigated
     * @param  default The size to use if one can't be calculated
     * @return the minimum size
     */
    internal fun minimumSize(view: View, default: Size = Empty): Size = minimumSize(PositionableWrapper(view), default)

    /**
     * Returns the ideal size of the [View] based on its contents.
     *
     * @param  view The View being investigated
     * @param  default The size to use if one can't be calculated
     * @return the ideal size
     */
    internal fun idealSize(view: View, default: Size? = null): Size? = idealSize(PositionableWrapper(view), default)

    /**
     * Gets the child within the [View] at the given point.
     *
     * @param of the View
     * @param at The point
     * @return The child (null if no child contains the given point)
     */
    internal fun child(of: View, at: Point): View? = child(PositionableWrapper(of), at)
}