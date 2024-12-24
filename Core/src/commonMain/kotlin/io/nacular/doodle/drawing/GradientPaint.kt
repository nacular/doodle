package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times

/**
 * A gradient [Paint] that transitions between a list of [Stop]s.
 *
 * @property colors at stop points along the transition
 *
 * @constructor
 * @param colors at stop points
 */
public sealed class GradientPaint(public val colors: List<Stop>): Paint() {
    public class Stop(public val color: Color, public val offset: Float)

    /**
     * Creates a fill with a gradient between the given colors.
     *
     * @param color1 associated with the start point
     * @param color2 associated with the end point
     */
    protected constructor(color1: Color, color2: Color): this(listOf(Stop(color1, 0f), Stop(color2, 1f)))

    /** `true` IFF any one of [colors] is visible */
    override val visible: Boolean = colors.any { it.color.visible }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GradientPaint) return false

        if (colors != other.colors) return false

        return true
    }

    override fun hashCode(): Int = colors.hashCode()
}

/**
 * A linear gradient [Paint] that transitions between a list of [Stop]s.
 *
 * Created by Nicholas Eddy on 11/5/18.
 *
 * @property start of the line along which the gradient flows
 * @property end of the line along which the gradient flows
 *
 * @constructor
 * @param colors at stop points
 * @param start of the line along which the gradient flows
 * @param end of the line along which the gradient flows
 */
public class LinearGradientPaint(colors: List<Stop>, public val start: Point, public val end: Point): GradientPaint(colors) {
    /**
     * Creates a fill with a gradient between the given colors.
     *
     * @param color1 associated with the start point
     * @param color2 associated with the end point
     * @param start of the line along which the gradient flows
     * @param end of the line along which the gradient flows
     */
    public constructor(color1: Color, color2: Color, start: Point, end: Point): this(listOf(Stop(color1, 0f), Stop(color2, 1f)), start, end)

    /** `true` IFF super visible and start != end */
    override val visible: Boolean = super.visible && start != end

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LinearGradientPaint) return false
        if (!super.equals(other)) return false

        if (start != other.start) return false
        if (end   != other.end  ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }
}

/**
 * A radial gradient [Paint] that transitions between a list of [Stop]s.
 *
 * @property start circle from which the gradient flows
 * @property end circle that the gradient stops at
 *
 * @constructor
 * @param colors at stop points
 * @param start circle from which the gradient flows
 * @param end circle that the gradient stops at
 */
public class RadialGradientPaint(colors: List<Stop>, public val start: Circle, public val end: Circle): GradientPaint(colors) {
    /**
     * Creates a fill with a gradient between the given colors.
     *
     * @param color1 associated with the start point
     * @param color2 associated with the end point
     * @param start circle from which the gradient flows
     * @param end circle that the gradient stops at
     */
    public constructor(color1: Color, color2: Color, start: Circle, end: Circle): this(listOf(Stop(color1, 0f), Stop(color2, 1f)), start, end)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RadialGradientPaint) return false
        if (!super.equals(other)) return false

        if (start != other.start) return false
        if (end   != other.end  ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }
}

/**
 * A [Paint] based on sweeping a "line" around a [center] point, through a series of [Stop]s to form what looks like a cone.
 *
 * @property center   of the cone
 * @property rotation of the starting line
 *
 * @param colors   at stop angles
 * @param center   of the cone
 * @param rotation of the starting line
 */
public class SweepGradientPaint(
               colors  : List<Stop>,
    public val center  : Point,
    public val rotation: Measure<Angle> = 0 * degrees
): GradientPaint(colors) {
    /**
     * Creates a conic with a gradient between the given colors.
     *
     * @param color1 at the start angle
     * @param color2 at the end of the sweep
     * @param center of the cone
     * @param rotation of the start angle
     */
    public constructor(
        color1  : Color,
        color2  : Color,
        center  : Point,
        rotation: Measure<Angle> = 0 * degrees
    ): this(listOf(Stop(color1, 0f), Stop(color2, 1f)), center, rotation)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SweepGradientPaint) return false
        if (!super.equals(other)) return false

        if (center   != other.center  ) return false
        if (rotation != other.rotation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + center.hashCode()
        result = 31 * result + rotation.hashCode()
        return result
    }
}