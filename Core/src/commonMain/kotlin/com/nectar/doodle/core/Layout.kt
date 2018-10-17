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
    val parent     : Gizmo?      // TODO: Should this be a Positionable?
    val children   : List<Gizmo> // TODO: Should this be a List<Positionable>?
    var idealSize  : Size?
    var minimumSize: Size
}

class PositionableWrapper(val gizmo: Gizmo): Positionable {
    override var size        get() = gizmo.size
        set(value) { gizmo.size = value }
    override val width       get() = gizmo.width
    override val height      get() = gizmo.height
    override val insets      get() = gizmo.insets_
    override val parent      get() = gizmo.parent
    override val children    get() = gizmo.children_
    override var idealSize   get() = gizmo.idealSize
        set(value) { gizmo.idealSize = value }
    override var minimumSize get() = gizmo.minimumSize
        set(value) { gizmo.minimumSize = value }
}

/**
 * Layouts control the positioning of a [Gizmo]'s children. They are also responsible for reporting the ideal size for a gizmo given it's contents.
 *
 * Layouts automatically take control of content positioning; therefore they should be used in preference of manually monitoring a Gizmo's size.
 *
 * A Gizmo's Layout will be asked to perform positioning whenever that Gizmo's size changes or it becomes visible after one or more of its
 * children has triggered a layout.  A child will trigger a layout if its bounds change or if it changes visibility.
 *
 * @author Nicholas Eddy
 */
abstract class Layout {
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
    open fun child(of: Positionable, at: Point): Gizmo? = null

    /**
     * Indicates that this Layout takes the ideal size of the Gizmo's
     * children into account when positioning them.
     *
     * This will be invoked before calling [Layout.layout],
     * allowing optimization for Layouts that ignore this property.
     *
     * @return true if the Layout takes child ideal size into account when sizing it
     */
    open val usesChildIdealSize: Boolean get() = false

    /**
     * Indicates that this Layout takes the minimum size of the Gizmo's
     * children into account when positioning them.
     *
     * This will be invoked before calling [Layout.layout],
     * allowing optimization for Layouts that ignore this property.
     *
     * @return true if the Layout takes child minimum size into account when sizing it
     */
    open val usesChildMinimumSize: Boolean get() = false

    /**
     * Causes the Layout to position the children of the given Gizmo. NOTE: stateful layouts will only
     * position the items they track, while stateless Layouts will iterate over the Gizmo's children.
     *
     * @param gizmo The Gizmo to lay out
     */
    internal fun layout(gizmo: Gizmo) = layout(PositionableWrapper(gizmo))

    /**
     * Returns the minimum size of the Gizmo based on its contents.
     *
     * @param  gizmo The Gizmo being investigated
     * @param  default The size to use if one can't be calculated
     * @return the minimum size
     */
    internal fun minimumSize(gizmo: Gizmo, default: Size = Empty): Size = minimumSize(PositionableWrapper(gizmo), default)

    /**
     * Returns the ideal size of the Gizmo based on its contents.
     *
     * @param  gizmo The Gizmo being investigated
     * @param  default The size to use if one can't be calculated
     * @return the ideal size
     */
    internal fun idealSize(gizmo: Gizmo, default: Size? = null): Size? = idealSize(PositionableWrapper(gizmo), default)

    /**
     * Gets the child within the Gizmo at the given point.
     *
     * @param of the Gizmo
     * @param at The point
     * @return The child (null if no child contains the given point)
     */
    internal fun child(of: Gizmo, at: Point): Gizmo? = child(PositionableWrapper(of), at)
}
