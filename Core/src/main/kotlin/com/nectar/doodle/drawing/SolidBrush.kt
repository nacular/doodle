package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Color.Companion.black


class SolidBrush(val color: Color): Brush() {

    override val visible = color.opacity > 0

    companion object {
        val Default = SolidBrush(black)
    }
}
