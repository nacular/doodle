package com.nectar.doodle.drawing

import com.nectar.doodle.core.Gizmo


interface GraphicsDevice<T: GraphicsSurface> {
    /**
     * @param  gizmo
     * @return the graphics surface for this [Gizmo], creating it if necessary
     */
    operator fun get(gizmo: Gizmo): T

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
     * Releases the [GraphicsSurface] associated with [Gizmo] and its children if applicable.
     *
     * @param  gizmo
     */
    fun release(gizmo: Gizmo)

    /**
     * Releases the given [GraphicsSurface] and its children if applicable.
     *
     * @param  surface
     */
    fun release(surface: T)
}