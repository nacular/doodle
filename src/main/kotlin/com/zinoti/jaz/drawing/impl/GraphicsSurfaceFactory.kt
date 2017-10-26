package com.zinoti.jaz.drawing.impl


import com.zinoti.jaz.dom.HtmlFactory
import com.zinoti.jaz.drawing.CanvasFactory
import com.zinoti.jaz.drawing.GraphicsSurface
import org.w3c.dom.HTMLElement


interface GraphicsSurfaceFactory<T: GraphicsSurface> {
    fun surface(element: HTMLElement? = null): T

    fun surface(parent: T? = null, isContainer: Boolean = false): T
}


class RealGraphicsSurfaceFactory(
        private val htmlFactory  : HtmlFactory,
        private val canvasFactory: CanvasFactory): GraphicsSurfaceFactory<RealGraphicsSurface> {
    override fun surface(element: HTMLElement?) =
            element?.let { RealGraphicsSurface(htmlFactory, canvasFactory, it) } ?: RealGraphicsSurface(htmlFactory, canvasFactory)

    override fun surface(parent: RealGraphicsSurface?, isContainer: Boolean) = RealGraphicsSurface(htmlFactory, canvasFactory, parent, isContainer)
}