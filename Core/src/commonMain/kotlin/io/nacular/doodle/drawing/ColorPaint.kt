package io.nacular.doodle.drawing


/**
 * A basic [Paint] consisting of a single [Color].
 *
 * @author Nicholas Eddy
 *
 * @property color of the paint
 */
public class ColorPaint(public val color: Color): Paint() {

    /** `false` if [color].[opacity][Color.opacity] == 0 */
    override val visible: Boolean = color.opacity > 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorPaint) return false

        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int = color.hashCode()
}

/**
 * Creates a new [ColorPaint] from the given [Color].
 */
public inline val Color.paint: ColorPaint get() = ColorPaint(this)