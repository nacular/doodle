package com.nectar.doodle.drawing.impl

import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer
import com.nectar.doodle.drawing.Shadow
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Rectangle


/**
 * VectorRenderers provide vector rendering implementations.
 */

interface VectorRenderer: Renderer {
    fun rect(rectangle: Rectangle,           brush: Brush        )
    fun rect(rectangle: Rectangle, pen: Pen, brush: Brush? = null)

    fun rect(rectangle: Rectangle, radius: Double,           brush: Brush)
    fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush? = null)

    fun circle(circle: Circle,           brush: Brush        )
    fun circle(circle: Circle, pen: Pen, brush: Brush? = null)

    fun ellipse(ellipse: Ellipse,           brush: Brush        )
    fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush? = null)

//    fun clip(rectangle: Rectangle, block: VectorRenderer.() -> Unit)

    fun add   (shadow: Shadow)
    fun remove(shadow: Shadow)
}

typealias VectorRendererFactory = (CanvasContext) -> VectorRenderer