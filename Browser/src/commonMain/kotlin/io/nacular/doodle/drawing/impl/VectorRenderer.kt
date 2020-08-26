package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Fill
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.text.StyledText


/**
 * VectorRenderers provide vector rendering implementations.
 */

internal interface VectorRenderer: Renderer {
    fun rect(rectangle: Rectangle,           fill: Fill        )
    fun rect(rectangle: Rectangle, stroke: Stroke, fill: Fill? = null)

    fun rect(rectangle: Rectangle, radius: Double,           fill: Fill)
    fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Fill? = null)

    fun circle(circle: Circle,           fill: Fill        )
    fun circle(circle: Circle, stroke: Stroke, fill: Fill? = null)

    fun ellipse(ellipse: Ellipse,           fill: Fill        )
    fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Fill? = null)

    fun text(text: String, font: Font? = null, at: Point, fill: Fill)

    fun text(text: StyledText, at: Point)

    fun wrapped(
            text       : String,
            font       : Font? = null,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double,
            fill      : Fill)

    fun wrapped(
            text       : StyledText,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double)
}

internal typealias VectorRendererFactory = (CanvasContext) -> VectorRenderer