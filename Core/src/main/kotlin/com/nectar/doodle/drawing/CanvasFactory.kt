package com.nectar.doodle.drawing

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactoryImpl
import com.nectar.doodle.drawing.impl.CanvasImpl
import com.nectar.doodle.drawing.impl.VectorRendererSvg
import org.w3c.dom.Node

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
interface CanvasFactory {
    operator fun invoke(region: Node): Canvas
}

class DefaultCanvasFactory(private val htmlFactory: HtmlFactory): CanvasFactory {
    override operator fun invoke(region: Node): Canvas {
        return CanvasImpl(region, htmlFactory) {
            VectorRendererSvg(it, SvgFactoryImpl())
        }
    }
}