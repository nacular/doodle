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

/**
 * Creates a new [ColorPaint] like this one except with the given opacity.
 *
 * @param value of the new opacity
 */
public infix fun ColorPaint.opacity(value: Float): ColorPaint = (color opacity value).paint

/**
 * Makes this [ColorPaint] lighter by the given percent.
 *
 * @param percent to lighten the paint
 */
public fun ColorPaint.lighter(percent: Float = 0.5f): ColorPaint = (color.lighter(percent)).paint

/**
 * Makes this [ColorPaint] darker by the given percent.
 *
 * @param percent to darken the paint
 */
public fun ColorPaint.darker(percent: Float = 0.5f): ColorPaint = (color.darker(percent)).paint

/**
 * @return a gray scale version of this [ColorPaint]
 */
public fun ColorPaint.grayScale(): ColorPaint = (color.grayScale()).paint