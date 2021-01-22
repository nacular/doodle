package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Color.Companion.Black

/**
 * Strokes are used to outline regions when drawing shapes on a [Canvas].
 *
 * @property color of outline
 * @property thickness of outline
 * @property dashes if lines are segmented
 *
 * @author Nicholas Eddy
 *
 * @constructor
 * @param color of outline
 * @param thickness of outline
 * @param dashes if lines are segmented
 */
public class Stroke(public val color: Color = Black, public val thickness: Double = 1.0, public val dashes: IntArray? = null) {
    public constructor(color: Color = Black, thickness: Double = 1.0, dash: Int, vararg remainingDashes: Int): this(color, thickness, intArrayOf(dash) + remainingDashes)

    /** `true` IFF [thickness] > 0 and [color] visible */
    public val visible: Boolean = thickness > 0 && color.visible
}