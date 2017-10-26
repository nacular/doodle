package com.zinoti.jaz.drawing

import com.zinoti.jaz.dom.SvgFactoryImpl
import com.zinoti.jaz.drawing.impl.CanvasImpl
import com.zinoti.jaz.drawing.impl.VectorRendererSvg
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