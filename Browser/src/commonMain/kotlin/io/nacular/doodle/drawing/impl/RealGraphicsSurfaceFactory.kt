package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.drawing.CanvasFactory


internal class RealGraphicsSurfaceFactory(
        private val htmlFactory  : HtmlFactory,
        private val canvasFactory: CanvasFactory): GraphicsSurfaceFactory<RealGraphicsSurface> {

    private val nonPopupTopLevelSurfaces = mutableListOf<RealGraphicsSurface>()

    operator fun invoke(element: HTMLElement) = RealGraphicsSurface(htmlFactory, canvasFactory, element, nonPopupTopLevelSurfaces)

    override operator fun invoke(): RealGraphicsSurface = RealGraphicsSurface(htmlFactory, canvasFactory, htmlFactory.create(), nonPopupTopLevelSurfaces)

    override operator fun invoke(parent: RealGraphicsSurface?, view: View, isContainer: Boolean, addToRootIfNoParent: Boolean) = RealGraphicsSurface(htmlFactory, canvasFactory, nonPopupTopLevelSurfaces, parent, view, isContainer, addToRootIfNoParent)
}