package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size


class PatternBrush(val size: Size, val fill: Canvas.() -> Unit): Brush() {
    override val visible = !size.empty
}

fun stripedBrush(rowHeight: Double, evenRowColor: Color?, oddRowColor: Color?) = PatternBrush(Size(rowHeight, 2 * rowHeight)) {
    evenRowColor?.let { rect(Rectangle(                rowHeight, rowHeight), ColorBrush(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(it)) }
}
