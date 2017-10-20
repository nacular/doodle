//package com.zinoti.jaz.core
//
//import com.zinoti.jaz.geometry.Dimension
//import com.zinoti.jaz.geometry.Point
//
//
///**
// * Layouts control the positioning of a [Container]'s children.
// * It is also responsible for reporting the ideal size for a container
// * given it's contents.
// *
// * Layouts automatically take control of content positioning; therefore
// * they should be used in preference of manually monitoring a Container's
// * size.
// *
// * A Container's Layout will be asked to perform positioning whenever that
// * Container's size changes or it becomes visible after one or more of its
// * children has triggered a layout.  A child will trigger a layout if its
// * bounds change or if it changes visibility.
// *
// * @author Nicholas Eddy
// */
//
//interface Layout {
//    /**
//     * Called whenever a Layout is installed into a Container. Layouts can use
//     * this notification to perform any initialization.
//     *
//     * @param container
//     */
//
//    fun install(container: Container)
//
//    /**
//     * Called whenever a Layout is removed from a Container.  Layouts should use
//     * this notification to undue any operations that took place in [Layout.install]
//     *
//     * @param container
//     */
//
//    fun uninstall(container: Container)
//
//    /**
//     * Causes the Layout to position the children of the given Container. NOTE: stateful layouts will only
//     * position the items they track, while stateless Layouts will iterate over the Container's children.
//     *
//     * @param container The Container to lay out
//     */
//
//    fun layout(container: Container)
//
//    /**
//     * Returns the minimum size of the Container based on its contents.
//     *
//     * @param  container The Container being investigated
//     * @return the minimum size
//     */
//
//    fun minimumSize(container: Container): Dimension? = null
//
//    /**
//     * Returns the ideal size of the Container based on its contents.
//     *
//     * @param  container The Container being investigated
//     * @return the ideal size
//     */
//
//    fun idealSize(container: Container): Dimension? = null
//
//    /**
//     * Gets the Gizmo within the Container at the given point.
//     *
//     * @param container the Container
//     * @param point The point
//     * @return The child (null if no child contains the given point)
//     */
//
//    fun childAtPoint(container: Container, point: Point): Gizmo? = null
//
//    /**
//     * Indicates that this Layout takes the ideal size of the Container's
//     * children into account when positioning them.
//     *
//     * This will be invoked before calling [Layout.layout],
//     * allowing optimization for Layouts that ignore this property.
//     *
//     * @return true if the Layout takes child ideal size into account when sizing it
//     */
//
//    val usesChildIdealSize: Boolean get() = false
//
//    /**
//     * Indicates that this Layout takes the minimum size of the Container's
//     * children into account when positioning them.
//     *
//     * This will be invoked before calling [Layout.layout],
//     * allowing optimization for Layouts that ignore this property.
//     *
//     * @return true if the Layout takes child minimum size into account when sizing it
//     */
//
//    val usesChildMinimumSize: Boolean get() = false
//}
