package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Color.Companion.Black

/**
 * Strokes are used to outline regions when drawing shapes on a [Canvas].
 *
 * @property color of outline
 * @property thickness of outline
 * @property dashes if lines are segmented
 * @property dashOffset of the dashes
 *
 * @author Nicholas Eddy
 *
 * @constructor
 * @param color of outline
 * @param thickness of outline
 * @param dashes if lines are segmented
 * @param dashOffset of the dashes
 */
public class Stroke(
        public val color     : Color        = Black,
        public val thickness : Double       = 1.0,
        public val dashes    : DoubleArray? = null,
        public val dashOffset: Double       = 0.0
) {
    public constructor(
            color     : Color  = Black,
            thickness : Double = 1.0,
            dashOffset: Double = 0.0,
            dash      : Double,
            vararg remainingDashes: Double
    ): this(color, thickness, doubleArrayOf(dash) + remainingDashes, dashOffset)

    /** `true` IFF [thickness] > 0 and [color] visible */
    public val visible: Boolean = thickness > 0 && color.visible

    public companion object {
        @Deprecated("Use new constructor")
        public operator fun invoke(
                color     : Color     = Black,
                thickness : Double    = 1.0,
                dashes    : IntArray? = null): Stroke = Stroke(color, thickness, dashes?.map { it.toDouble() }?.toDoubleArray())

        @Deprecated("Use new constructor")
        public operator fun invoke(
                color     : Color     = Black,
                thickness : Double    = 1.0,
                dash      : Int,
                vararg remainingDashes: Int): Stroke = Stroke(color, thickness, doubleArrayOf(dash.toDouble()) + remainingDashes.map { it.toDouble() }.toDoubleArray())
    }
}