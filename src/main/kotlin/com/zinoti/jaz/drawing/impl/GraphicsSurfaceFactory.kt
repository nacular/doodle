package com.zinoti.jaz.drawing.impl


import com.zinoti.jaz.drawing.CanvasFactory
import com.zinoti.jaz.drawing.GraphicsSurface
import org.w3c.dom.HTMLElement


interface GraphicsSurfaceFactory<T: GraphicsSurface> {
    fun surface(element: HTMLElement? = null): T

    fun surface(parent: T? = null, isContainer: Boolean = false): T
}


class RealGraphicsSurfaceFactory(private val canvasFactory: CanvasFactory): GraphicsSurfaceFactory<RealGraphicsSurface> {
    override fun surface(element: HTMLElement?): RealGraphicsSurface = element?.let { RealGraphicsSurface(canvasFactory, it) } ?: RealGraphicsSurface(canvasFactory)

    override fun surface(parent: RealGraphicsSurface?, isContainer: Boolean): RealGraphicsSurface {
        return RealGraphicsSurface(canvasFactory, parent, isContainer)
    }
}