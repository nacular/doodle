package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Color.Companion.Black

/**
 * Strokes are used to outline regions when drawing shapes on a [Canvas].
 *
 * @property fill that paints the outline
 * @property thickness of outline
 * @property dashes if lines are segmented
 * @property dashOffset of the dashes
 *
 * @author Nicholas Eddy
 *
 * @constructor
 * @param fill that paints the outline
 * @param thickness of outline
 * @param dashes if lines are segmented
 * @param dashOffset of the dashes
 */
public class Stroke(
        public val fill      : Paint        = ColorPaint(Black),
        public val thickness : Double       = 1.0,
        public val dashes    : DoubleArray? = null,
        public val dashOffset: Double       = 0.0
) {
    /** `true` IFF [thickness] > 0 and [color] visible */
    public val visible: Boolean = thickness > 0 && fill.visible

    public companion object {
        public operator fun invoke(
                color     : Color  = Black,
                thickness : Double = 1.0,
                dashOffset: Double = 0.0,
                dashes    : DoubleArray
        ): Stroke = Stroke(ColorPaint(color), thickness, dashes, dashOffset)

        public operator fun invoke(
                color     : Color  = Black,
                thickness : Double = 1.0,
                dashOffset: Double = 0.0,
                dash      : Double,
                vararg remainingDashes: Double
        ): Stroke = Stroke(ColorPaint(color), thickness, doubleArrayOf(dash) + remainingDashes, dashOffset)

        public operator fun invoke(color: Color, thickness: Double = 1.0): Stroke = Stroke(ColorPaint(color), thickness, null)
    }
}