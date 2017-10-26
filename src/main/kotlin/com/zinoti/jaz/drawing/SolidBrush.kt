package com.zinoti.jaz.drawing

import com.zinoti.jaz.drawing.Color.Companion.black


class SolidBrush(val color: Color): Brush() {

    override val visible = color.opacity > 0

    companion object {
        val Default = SolidBrush(black)
    }
}
