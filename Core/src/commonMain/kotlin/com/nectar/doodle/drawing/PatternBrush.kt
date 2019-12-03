package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size


/**
 * A [Brush] that repeats the contents drawn to a [Canvas].
 *
 * @author Nicholas Eddy
 *
 * @property size The size of the Canvas that will be repeated
 * @property fill Specifies the render operations for the Canvas
 */
class PatternBrush(val size: Size, val fill: Canvas.() -> Unit): Brush() {
    override val visible = !size.empty
}

/**
 * Creates a [PatternBrush] that draws an alternating horizontal striped pattern.
 */
fun horizontalStripedBrush(rowHeight: Double, evenRowColor: Color?, oddRowColor: Color?) = PatternBrush(Size(rowHeight, 2 * rowHeight)) {
    evenRowColor?.let { rect(Rectangle(                rowHeight, rowHeight), ColorBrush(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(it)) }
}

/**
 * Creates a [PatternBrush] that draws an alternating vertical striped pattern.
 */
fun verticalStripedBrush(colWidth: Double, evenRowColor: Color?, oddRowColor: Color?) = PatternBrush(Size(2 * colWidth, colWidth)) {
    evenRowColor?.let { rect(Rectangle(               colWidth, colWidth), ColorBrush(it)) }
    oddRowColor?.let  { rect(Rectangle(colWidth, 0.0, colWidth, colWidth), ColorBrush(it)) }
}
