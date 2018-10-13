package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactoryImpl
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.TextFactory
import org.w3c.dom.HTMLElement


internal class CanvasFactoryImpl(private val htmlFactory: HtmlFactory, private val textFactory: TextFactory): CanvasFactory {
    override operator fun invoke(region: HTMLElement) = CanvasImpl(region, htmlFactory, textFactory) {
        VectorRendererSvg(it, SvgFactoryImpl())
    }
}