package io.nacular.doodle.drawing

import io.nacular.doodle.core.View


interface GraphicsDevice<T: GraphicsSurface> {
    /**
     * @param  view
     * @return the graphics surface for this [View], creating it if necessary
     */
    operator fun get(view: View): T

    fun create(view: View): T

    /**
     * @return a top-level graphics surface
     */
    fun create(): T

    /**
     * Releases the [GraphicsSurface] associated with [View] and its children if applicable.
     *
     * @param view
     */
    fun release(view: View)

    /**
     * Releases the given [GraphicsSurface] and its children if applicable.
     *
     * @param surface
     */
    fun release(surface: T)
}