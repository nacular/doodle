package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Size


class CanvasBrush(val size: Size, val fill: Canvas.() -> Unit): Brush() {
    override val visible = !size.empty
}