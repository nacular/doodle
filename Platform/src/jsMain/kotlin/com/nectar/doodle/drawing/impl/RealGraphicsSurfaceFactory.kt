package com.nectar.doodle.drawing.impl


import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.CanvasFactory
import org.w3c.dom.HTMLElement


internal class RealGraphicsSurfaceFactory(
        private val htmlFactory  : HtmlFactory,
        private val canvasFactory: CanvasFactory): GraphicsSurfaceFactory<RealGraphicsSurface> {

    fun surface(element: HTMLElement?) =
            element?.let { RealGraphicsSurface(htmlFactory, canvasFactory, it) } ?: RealGraphicsSurface(htmlFactory, canvasFactory)

    override operator fun invoke(parent: RealGraphicsSurface?, isContainer: Boolean) = RealGraphicsSurface(htmlFactory, canvasFactory, parent, isContainer)
}