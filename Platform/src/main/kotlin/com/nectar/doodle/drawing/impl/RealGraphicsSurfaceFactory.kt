package com.nectar.doodle.drawing.impl


import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.CanvasFactory


class RealGraphicsSurfaceFactory(
        private val htmlFactory  : HtmlFactory,
        private val canvasFactory: CanvasFactory): GraphicsSurfaceFactory<RealGraphicsSurface> {
//    override fun surface(element: HTMLElement?) =
//            element?.let { RealGraphicsSurface(htmlFactory, canvasFactory, it) } ?: RealGraphicsSurface(htmlFactory, canvasFactory)

    override fun surface(parent: RealGraphicsSurface?, isContainer: Boolean) = RealGraphicsSurface(htmlFactory, canvasFactory, parent, isContainer)
}