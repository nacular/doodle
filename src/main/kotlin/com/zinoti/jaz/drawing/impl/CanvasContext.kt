package com.zinoti.jaz.drawing.impl

import com.zinoti.jaz.drawing.AffineTransform
import com.zinoti.jaz.drawing.Renderer
import com.zinoti.jaz.geometry.Size
import org.w3c.dom.Node

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

interface CanvasContext {
    var size          : Size
    var transform     : AffineTransform
    var optimization  : Renderer.Optimization
    val renderRegion  : Node
    var renderPosition: Node?
}