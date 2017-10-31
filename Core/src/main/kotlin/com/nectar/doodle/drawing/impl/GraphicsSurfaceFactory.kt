package com.nectar.doodle.drawing.impl


import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.GraphicsSurface
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