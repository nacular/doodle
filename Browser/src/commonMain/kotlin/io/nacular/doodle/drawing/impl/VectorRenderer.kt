package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Brush
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Pen
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.text.StyledText


/**
 * VectorRenderers provide vector rendering implementations.
 */

internal interface VectorRenderer: Renderer {
    fun rect(rectangle: Rectangle,           brush: Brush        )
    fun rect(rectangle: Rectangle, pen: Pen, brush: Brush? = null)

    fun rect(rectangle: Rectangle, radius: Double,           brush: Brush)
    fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush? = null)

    fun circle(circle: Circle,           brush: Brush        )
    fun circle(circle: Circle, pen: Pen, brush: Brush? = null)

    fun ellipse(ellipse: Ellipse,           brush: Brush        )
    fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush? = null)

    fun text(text: String, font: Font? = null, at: Point, brush: Brush)

    fun text(text: StyledText, at: Point)

    fun wrapped(
            text       : String,
            font       : Font? = null,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double,
            brush      : Brush)

    fun wrapped(
            text       : StyledText,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double)

//    fun clip(rectangle: Rectangle, block: VectorRenderer.() -> Unit)

    fun add   (shadow: Shadow)
    fun remove(shadow: Shadow)
}

internal typealias VectorRendererFactory = (CanvasContext) -> VectorRenderer