package com.nectar.doodle.drawing


class ColorBrush(val color: Color): Brush() {
    override val visible = color.opacity > 0
}
