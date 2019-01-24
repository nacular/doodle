package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.TextFactory
import org.w3c.dom.HTMLElement


internal class CanvasFactoryImpl(private val htmlFactory            : HtmlFactory,
                                 private val textFactory            : TextFactory,
                                 private val vectorBackgroundFactory: VectorBackgroundFactory,
                                 private val svgFactory             : SvgFactory): CanvasFactory {
    override operator fun invoke(region: HTMLElement) = CanvasImpl(region, htmlFactory, textFactory, vectorBackgroundFactory) {
        VectorRendererSvg(it, svgFactory, vectorBackgroundFactory)
    }
}