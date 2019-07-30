package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 11/5/18.
 */
class LinearGradientBrush(val colors: List<Stop>, val start: Point, val end: Point): Brush() {
    data class Stop(val color: Color, val offset: Float)

    constructor(color1: Color, color2: Color, start: Point, end: Point): this(listOf(Stop(color1, 0f), Stop(color2, 1f)), start, end)

    override val visible = colors.any { it.color.opacity > 0 }
}
