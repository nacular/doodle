package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextMetrics


internal class CanvasFactoryImpl(private val htmlFactory: HtmlFactory,
                                 private val textFactory: TextFactory,
                                 private val svgFactory : SvgFactory,
                                 private val textMetrics: TextMetrics): CanvasFactory {
    override operator fun invoke(region: HTMLElement) = CanvasImpl(region, htmlFactory, textFactory) {
        VectorRendererSvg(it, svgFactory, htmlFactory, textMetrics)
    }
}