package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Color.Companion.black

/**
 * Pens are used to outline regions when drawing shapes on a [Canvas].
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
class Pen(val color: Color = black, val thickness: Double = 1.0, val dashes: IntArray? = null) {
    constructor(color: Color = black, thickness: Double = 1.0, dash: Int, vararg remainingDashes: Int): this(color, thickness, intArrayOf(dash) + remainingDashes)

    /** `true` IFF [thickness] > 0 and [color] visible */
    val visible = thickness > 0 && color.visible
}