package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.geometry.times


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
 *
 * @param rowHeight    Height of the alternating rows
 * @param evenRowColor Color used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor Color used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
fun horizontalStripedBrush(rowHeight: Double, evenRowColor: Color? = null, oddRowColor: Color? = null) = PatternBrush(Size(if (evenRowColor.visible || oddRowColor.visible) rowHeight else 0.0, 2 * rowHeight)) {
    evenRowColor?.let { rect(Rectangle(                rowHeight, rowHeight), ColorBrush(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(it)) }
}

/**
 * Creates a [PatternBrush] that draws an alternating vertical striped pattern.
 *
 * @param colWidth     Width of the alternating columns
 * @param evenRowColor Color used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor Color used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
fun verticalStripedBrush(colWidth: Double, evenRowColor: Color? = null, oddRowColor: Color? = null) = PatternBrush(Size(2 * colWidth, if (evenRowColor.visible || oddRowColor.visible) colWidth else 0.0)) {
    evenRowColor?.let { rect(Rectangle(               colWidth, colWidth), ColorBrush(it)) }
    oddRowColor?.let  { rect(Rectangle(colWidth, 0.0, colWidth, colWidth), ColorBrush(it)) }
}

/**
 * Creates a [PatternBrush] that draws a checkered pattern.
 *
 * @param checkerSize Size of each rectangle in the checker pattern
 * @param firstColor  Color of the first rectangle, left-to-right
 * @param secondColor Color of the second rectangle, left-to-right
 */
fun checkerBrush(checkerSize: Size, firstColor: Color? = null, secondColor: Color? = null) = PatternBrush(if (firstColor.visible || secondColor.visible) checkerSize * 2 else Empty) {
    val w  = checkerSize.width
    val h  = checkerSize.height
    val b1 = firstColor?.let  { ColorBrush(it) }
    val b2 = secondColor?.let { ColorBrush(it) }

    b1?.let { rect(Rectangle(0.0, 0.0, w, h), it) }
    b2?.let { rect(Rectangle(0.0,   h, w, h), it) }
    b2?.let { rect(Rectangle(w,   0.0, w, h), it) }
    b1?.let { rect(Rectangle(w,     h, w, h), it) }
}