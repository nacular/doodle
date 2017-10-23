package com.zinoti.jaz.drawing

import com.zinoti.jaz.core.Gizmo
import org.w3c.dom.HTMLElement


interface GraphicsDevice {
    /**
     * @param  gizmo
     * @return the graphics surface for this Gizmo
     */

    operator fun get(gizmo: Gizmo): GraphicsSurface

    /**
     * @return a top-level graphics surface
     */

    fun create(): GraphicsSurface

    /**
     * @param  aElement
     * @return a new GraphicsSurface backed by the given Element
     */

    fun create(aElement: HTMLElement): GraphicsSurface

    /**
     * Releases the GraphicsSurface associated with Gizmo and its
     * children if applicable.
     *
     * @param  gizmo
     */

    fun release(gizmo: Gizmo)

    /**
     * Releases the given GraphicsSurface and its
     * children if applicable.
     *
     * @param  surface
     */
    fun release(surface: GraphicsSurface)
}