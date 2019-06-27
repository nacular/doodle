package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Color.Companion.black


class Pen(val color: Color = black, val thickness: Double = 1.0, val dashes: IntArray? = null) {
    constructor(color: Color = black, thickness: Double = 1.0, dash: Int, vararg remainingDashes: Int): this(color, thickness, intArrayOf(dash) + remainingDashes)

    val visible = thickness > 0 && color.opacity > 0
}