package com.nectar.doodle.drawing

import com.nectar.doodle.dom.SvgFactoryImpl
import com.nectar.doodle.drawing.impl.CanvasImpl
import com.nectar.doodle.drawing.impl.VectorRendererSvg
import org.w3c.dom.Node

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
typealias CanvasFactory = (region: Node) -> Canvas

fun defaultCanvasFactory(region: Node): Canvas {
    return CanvasImpl(region) {
        VectorRendererSvg(it, SvgFactoryImpl())
    }
}