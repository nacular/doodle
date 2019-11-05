package com.nectar.doodle.drawing

import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle


interface RenderManager {
    /**
     * Renders the given [View] during the next render cycle.  This has no effect
     * if the given [View] is not currently added to the [Display][com.nectar.doodle.core.Display].
     *
     * @param view
     */
    fun render(view: View)

    /**
     * Renders the given [View] immediately, without waiting until the next render cycle.  This has no effect
     * if the given [View] is not currently added to the [Display][com.nectar.doodle.core.Display].
     *
     * @param view
     */
    fun renderNow(view: View)

    fun layout(view: View)

    fun layoutNow(view: View)

    /**
     * @param of
     * @return the [View]'s current display rectangle (in its coordinate system) based on clipping with ancestor display rectangles.
     */
    fun displayRect(of: View): Rectangle?
}
