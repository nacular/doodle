package io.nacular.doodle.drawing

import io.nacular.doodle.core.View


public interface GraphicsDevice<T: GraphicsSurface> {
    /**
     * @param  view
     * @return the graphics surface for this [View], creating it if necessary
     */
    public operator fun get(view: View): T

    public fun create(view: View): T

    /**
     * @return a top-level graphics surface
     */
    public fun create(): T

    /**
     * Releases the [GraphicsSurface] associated with [View] and its children if applicable.
     *
     * @param view
     */
    public fun release(view: View)

    /**
     * Releases the given [GraphicsSurface] and its children if applicable.
     *
     * @param surface
     */
    public fun release(surface: T)
}