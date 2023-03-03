package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.TextAlignment


/**
 * VectorRenderers provide vector rendering implementations.
 */

internal interface VectorRenderer: Renderer {
    fun rect(rectangle: Rectangle,           fill: Paint)
    fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint? = null)

    fun rect(rectangle: Rectangle, radius: Double,           fill: Paint)
    fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint? = null)

    fun circle(circle: Circle,           fill: Paint)
    fun circle(circle: Circle, stroke: Stroke, fill: Paint? = null)

    fun ellipse(ellipse: Ellipse,           fill: Paint)
    fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint? = null)

    fun text(text: String, font: Font? = null, at: Point, fill: Paint, letterSpacing: Double)

    fun text(text: StyledText, at: Point, letterSpacing: Double)

    fun wrapped(text: String, font: Font? = null, at: Point, leftMargin: Double, rightMargin: Double, fill: Paint, alignment: TextAlignment, lineSpacing: Float, letterSpacing: Double)

    fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double, alignment: TextAlignment, lineSpacing: Float, letterSpacing: Double)
}

internal typealias VectorRendererFactory = (CanvasContext) -> VectorRenderer