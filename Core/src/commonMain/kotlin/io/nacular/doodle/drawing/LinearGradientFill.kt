package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Point

/**
 * A fill that fills with a linear gradient that transitions between a list of [Stop]s.
 *
 * Created by Nicholas Eddy on 11/5/18.
 *
 * @property colors at stop points
 * @property start of the line along which the gradient flows
 * @property end of the line along which the gradient flows
 *
 * @constructor
 * @param colors at stop points
 * @param start of the line along which the gradient flows
 * @param end of the line along which the gradient flows
 */
class LinearGradientFill(val colors: List<Stop>, val start: Point, val end: Point): Fill() {
    data class Stop(val color: Color, val offset: Float)

    /**
     * Creates a fill with a gradient between the given colors.
     *
     * @param color1 associated with the start point
     * @param color2 associated with the end point
     * @param start of the line along which the gradient flows
     * @param end of the line along which the gradient flows
     */
    constructor(color1: Color, color2: Color, start: Point, end: Point): this(listOf(Stop(color1, 0f), Stop(color2, 1f)), start, end)

    /** `true` IFF any one of [colors] is visible */
    override val visible = colors.any { it.color.visible }
}
