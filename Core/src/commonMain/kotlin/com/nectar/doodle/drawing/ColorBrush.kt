package com.nectar.doodle.drawing


class ColorBrush(val color: Color): Brush() {
    override val visible = color.opacity > 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorBrush) return false

        if (color != other.color) return false

        return true
    }

    override fun hashCode() = color.hashCode()
}
