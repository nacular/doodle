package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size


/**
 * Layouts control the positioning of a [Gizmo]'s children.
 * It is also responsible for reporting the ideal size for a gizmo
 * given it's contents.
 *
 * Layouts automatically take control of content positioning; therefore
 * they should be used in preference of manually monitoring a Gizmo's
 * size.
 *
 * A Gizmo's Layout will be asked to perform positioning whenever that
 * Gizmo's size changes or it becomes visible after one or more of its
 * children has triggered a layout.  A child will trigger a layout if its
 * bounds change or if it changes visibility.
 *
 * @author Nicholas Eddy
 */

interface Layout {
//    /**
//     * Called whenever a Layout is installed into a Gizmo. Layouts can use
//     * this notification to perform any initialization.
//     *
//     * @param gizmo
//     */
//
//    fun install(gizmo: Gizmo)
//
//    /**
//     * Called whenever a Layout is removed from a Gizmo.  Layouts should use
//     * this notification to undue any operations that took place in [Layout.install]
//     *
//     * @param gizmo
//     */
//
//    fun uninstall(gizmo: Gizmo)

    /**
     * Causes the Layout to position the children of the given Gizmo. NOTE: stateful layouts will only
     * position the items they track, while stateless Layouts will iterate over the Gizmo's children.
     *
     * @param gizmo The Gizmo to lay out
     */
    fun layout(gizmo: Gizmo)

    /**
     * Returns the minimum size of the Gizmo based on its contents.
     *
     * @param  gizmo The Gizmo being investigated
     * @param  default The size to use if one can't be calculated
     * @return the minimum size
     */
    fun minimumSize(gizmo: Gizmo, default: Size = Size.Empty): Size = default

    /**
     * Returns the ideal size of the Gizmo based on its contents.
     *
     * @param  gizmo The Gizmo being investigated
     * @param  default The size to use if one can't be calculated
     * @return the ideal size
     */
    fun idealSize(gizmo: Gizmo, default: Size? = null): Size? = default

    /**
     * Gets the child within the Gizmo at the given point.
     *
     * @param of the Gizmo
     * @param at The point
     * @return The child (null if no child contains the given point)
     */
    fun child(of: Gizmo, at: Point): Gizmo? = null

    /**
     * Indicates that this Layout takes the ideal size of the Gizmo's
     * children into account when positioning them.
     *
     * This will be invoked before calling [Layout.layout],
     * allowing optimization for Layouts that ignore this property.
     *
     * @return true if the Layout takes child ideal size into account when sizing it
     */
    val usesChildIdealSize: Boolean get() = false

    /**
     * Indicates that this Layout takes the minimum size of the Gizmo's
     * children into account when positioning them.
     *
     * This will be invoked before calling [Layout.layout],
     * allowing optimization for Layouts that ignore this property.
     *
     * @return true if the Layout takes child minimum size into account when sizing it
     */
    val usesChildMinimumSize: Boolean get() = false
}
