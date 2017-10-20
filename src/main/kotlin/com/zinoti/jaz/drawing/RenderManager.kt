//package com.zinoti.jaz.drawing
//
//import com.zinoti.jaz.core.Gizmo
//import com.zinoti.jaz.geometry.Rectangle
//
//
//interface RenderManager {
//    /**
//     * Renders the given Gizmo during the next render cycle.
//     *
//     * @param  gizmo
//     */
//    fun render(gizmo: Gizmo)
//
//    /**
//     * Renders the given Gizmo immediately, without waiting until the next
//     * render cycle.
//     *
//     * @param  gizmo
//     */
//    fun renderNow(gizmo: Gizmo)
//
//    /**
//     * @param  gizmo
//     * @return the Gizmo's current display rectangle (in Gizmo's coordinate system) based on clipping
//     * with ancestor display rectangles.
//     */
//    fun getDisplayRect(gizmo: Gizmo): Rectangle
//}
