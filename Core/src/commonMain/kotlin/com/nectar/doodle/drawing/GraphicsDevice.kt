package com.nectar.doodle.drawing

import com.nectar.doodle.core.View


interface GraphicsDevice<T: GraphicsSurface> {
    /**
     * @param  view
     * @return the graphics surface for this [View], creating it if necessary
     */
    operator fun get(view: View): T

    /**
     * @return a top-level graphics surface
     */
    fun create(): T

//    /**
//     * @param  element
//     * @return a new GraphicsSurface backed by the given Element
//     */
//
//    fun create(element: HTMLElement): T

    /**
     * Releases the [GraphicsSurface] associated with [View] and its children if applicable.
     *
     * @param  view
     */
    fun release(view: View)

    /**
     * Releases the given [GraphicsSurface] and its children if applicable.
     *
     * @param  surface
     */
    fun release(surface: T)
}