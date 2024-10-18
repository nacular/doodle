package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.View
import io.nacular.doodle.core.impl.DisplayImpl
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.drawing.CanvasFactory


internal class RealGraphicsSurfaceFactory(
    private val htmlFactory  : HtmlFactory,
    private val canvasFactory: CanvasFactory,
    private val displayImpl  : DisplayImpl,
): GraphicsSurfaceFactory<RealGraphicsSurface> {

    private val nonPopupTopLevelSurfaces = mutableListOf<RealGraphicsSurface>()

    operator fun invoke(element: HTMLElement) = RealGraphicsSurface(
        htmlFactory              = htmlFactory,
        canvasFactory            = canvasFactory,
        element                  = element,
        nonPopupTopLevelSurfaces = nonPopupTopLevelSurfaces
    ) {
        0
    }

    override operator fun invoke(): RealGraphicsSurface = RealGraphicsSurface(
        htmlFactory              = htmlFactory,
        canvasFactory            = canvasFactory,
        element                  = htmlFactory.create(),
        nonPopupTopLevelSurfaces = nonPopupTopLevelSurfaces
    ) {
        0
    }

    override operator fun invoke(
        parent             : RealGraphicsSurface?,
        view               : View,
        isContainer        : Boolean,
        addToRootIfNoParent: Boolean
    ): RealGraphicsSurface = RealGraphicsSurface(
        htmlFactory              = htmlFactory,
        canvasFactory            = canvasFactory,
        nonPopupTopLevelSurfaces = nonPopupTopLevelSurfaces,
        parent                   = parent,
        view                     = view,
        isContainer              = isContainer,
        addToRootIfNoParent      = addToRootIfNoParent
    ) {
        displayImpl.renderOffset
    }
}