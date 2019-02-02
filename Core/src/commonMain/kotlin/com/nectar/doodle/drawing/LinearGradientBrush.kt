package com.nectar.doodle.drawing

import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure
import com.nectar.measured.units.degrees
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 11/5/18.
 */
class LinearGradientBrush(val colors: List<Stop>, val rotation: Measure<Angle> = 0 * degrees): Brush() {
    data class Stop(val color: Color, val offset: Float)

    constructor(color1: Color, color2: Color, rotation: Measure<Angle> = 0 * degrees): this(listOf(Stop(color1, 0f), Stop(color2, 1f)), rotation)

    override val visible = colors.any { it.color.opacity > 0 }
}
