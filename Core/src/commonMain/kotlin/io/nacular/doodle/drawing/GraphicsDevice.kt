package io.nacular.doodle.drawing

import io.nacular.doodle.core.View


public interface GraphicsDevice<T: GraphicsSurface> {
    /**
     * @param  view
     * @return the graphics surface for this [View], creating it if necessary
     */
    public operator fun get(view: View): T

    /**
     * @return `true` if the View has a [GraphicsSurface]
     */
    public operator fun contains(view: View): Boolean

    public fun create(view: View): T

    /**
     * @return a top-level graphics surface
     */
    public fun create(): T

    /**
     * Releases the [GraphicsSurface] associated with [view] and its children if applicable.
     *
     * @param view
     */
    public fun release(view: View)

    /**
     * Removes the [GraphicsSurface] associated with [view] and its children if applicable.
     * NOTE: This does NOT release the surface.
     *
     * @param view to remove
     * @return [view]'s surface if it exists
     * @see release
     */
    public fun remove(view: View): T?

    /**
     * Releases the given [GraphicsSurface] and its children if applicable.
     *
     * @param surface
     */
    public fun release(surface: T)
}