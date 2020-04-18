package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.CanvasFactory


internal class RealGraphicsSurfaceFactory(
        private val htmlFactory  : HtmlFactory,
        private val canvasFactory: CanvasFactory): GraphicsSurfaceFactory<RealGraphicsSurface> {

    operator fun invoke(element: HTMLElement) = RealGraphicsSurface(htmlFactory, canvasFactory, element)

    override operator fun invoke(): RealGraphicsSurface = RealGraphicsSurface(htmlFactory, canvasFactory, htmlFactory.create())

    override operator fun invoke(parent: RealGraphicsSurface?, view: View, isContainer: Boolean, addToRootIfNoParent: Boolean) = RealGraphicsSurface(htmlFactory, canvasFactory, parent, view, isContainer, addToRootIfNoParent)
}