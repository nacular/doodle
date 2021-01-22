package io.nacular.doodle.drawing


/**
 * A basic [Fill] consisting of a single [Color].
 *
 * @author Nicholas Eddy
 *
 * @property color The fill's color
 */
public class ColorFill(public val color: Color): Fill() {

    /** `false` if [color].[opacity][Color.opacity] == 0 */
    override val visible: Boolean = color.opacity > 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorFill) return false

        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int = color.hashCode()
}