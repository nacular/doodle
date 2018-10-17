package com.nectar.doodle.drawing

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Rectangle


interface RenderManager {
    /**
     * Renders the given Gizmo during the next render cycle.  This has no effect
     * if the given Gizmo is not currently added to the [Display][com.nectar.doodle.core.Display].
     *
     * @param gizmo
     */
    fun render(gizmo: Gizmo)

    /**
     * Renders the given Gizmo immediately, without waiting until the next render cycle.  This has no effect
     * if the given Gizmo is not currently added to the [Display][com.nectar.doodle.core.Display].
     *
     * @param gizmo
     */
    fun renderNow(gizmo: Gizmo)

    /**
     * @param of
     * @return the Gizmo's current display rectangle (in its coordinate system) based on clipping with ancestor display rectangles.
     */
    fun displayRect(of: Gizmo): Rectangle?
}
