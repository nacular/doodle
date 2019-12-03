package com.nectar.doodle.drawing


/**
 * A basic [Brush] consisting of a single [Color].
 *
 * @author Nicholas Eddy
 *
 * @property color The brush's color
 */
class ColorBrush(val color: Color): Brush() {

    /** `false` if [color].[opacity][Color.opacity] == 0 */
    override val visible = color.opacity > 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorBrush) return false

        if (color != other.color) return false

        return true
    }

    override fun hashCode() = color.hashCode()
}