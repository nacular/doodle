package io.nacular.doodle.geometry

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.min
import kotlin.math.sqrt

/**
 * A [Shape] defined by a set of line segments that connect to enclose a region.
 */
public abstract class Polygon: Shape {

    /** Points representing the vertices */
    public abstract val points: List<Point>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polygon) return false

        if (points != other.points) return false

        return true
    }

    override fun hashCode(): Int = points.hashCode()

    /**
     * Gives the smallest [Rectangle] that fully contains this Polygon.
     *
     * ```
     * ┌─────────────────────┐
     * │     **********      │
     * │   ************      │
     * │ **************      │
     * │*********************│
     * │*********************│
     * │******************** │
     * │*******************  │
     * │       ************  │
     * │        ***********  │
     * │         **********  │
     * └─────────────────────┘
     * ```
     */
    override val boundingRectangle: Rectangle by lazy {
        val minX = points.minByOrNull { it.x }!!.x
        val minY = points.minByOrNull { it.y }!!.y
        val maxX = points.maxByOrNull { it.x }!!.x
        val maxY = points.maxByOrNull { it.y }!!.y

        Rectangle(Point(minX, minY), Size(maxX - minX, maxY - minY))
    }
}

/**
 * A Polygon with internal angles all <= 180°
 */
public abstract class ConvexPolygon: Polygon() {
    private class Line(val start: Point, val end: Point)

    // https://en.wikipedia.org/wiki/Shoelace_formula
    override val area: Double by lazy {
        var area = 0.0              // Accumulates area in the loop
        var j    = points.size - 1  // The last vertex is the 'previous' one to the first
        var i    = 0

        while (i < points.size) {
            val ith = points[i]
            val jth = points[j]

            area += (jth.x + ith.x) * (jth.y - ith.y)
            j = i  //j is previous vertex to i
            i++
        }

        abs(area / 2)
    }

    override val empty: Boolean by lazy { area == 0.0 }

    /**
     * Uses winding-number approach
     * http://geomalgorithms.com/a03-_inclusion.html
     */
    override fun contains(point: Point): Boolean {
        var result = 0 // the winding number counter

        points.forEachIndexed { i, vertex ->
            val index = (i+1).rem(points.size)

            when {
                vertex.y <= point.y ->
                    if (points[index].y > point.y && isLeft(Line(vertex, points[index]), point) > 0) {
                        ++result
                    }
                    // have a valid up intersect
                else ->
                    if (points[index].y <= point.y && isLeft(Line(vertex, points[index]), point) < 0) {
                        --result
                    }
                    // have a valid down intersect
            }
        }

        return result != 0
    }

    /** @return ```true``` IFF the given rectangle falls within the boundaries of this Polygon */
    override fun contains(rectangle: Rectangle): Boolean = rectangle.points.all { contains(it) }

    override fun intersects(rectangle: Rectangle): Boolean = TODO("not implemented")

    private fun isLeft(line: Line, point: Point): Int = ((line.end.x - line.start.x) * (point.y - line.start.y) - (point.x - line.start.x) * (line.end.y - line.start.y)).toInt()

    public companion object {
        public operator fun invoke(first: Point, second: Point, third: Point): ConvexPolygon {
            return ConvexPolygonImpl(first, second, third)
        }

        // FIXME: Make this internal to avoid invalid "convex" poly creation
        public operator fun invoke(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon {
            return ConvexPolygonImpl(first, second, third, *remaining)
        }
    }
}

/**
 * Create a new [ConvexPolygon] with the same points, but in reversed order.
 */
public fun ConvexPolygon.reversed(): ConvexPolygon = ConvexPolygonImpl(points.reversed())

/**
 * Create a new [ConvexPolygon] with each point transformed by [transform].
 *
 * @param transform to apply to each point
 */
public fun ConvexPolygon.map(transform: (Point) -> Point): ConvexPolygon = ConvexPolygonImpl(points.map(transform))

private class ConvexPolygonImpl(override val points: List<Point>): ConvexPolygon() {
    constructor(first: Point, second: Point, third: Point, vararg remaining: Point): this(listOf(first, second, third) + remaining)
}

/**
 * Creates a [Regular polygon](https://en.wikipedia.org/wiki/Regular_polygon) by inscribing it within the circle.
 *
 * @param sides the polygon should have
 * @param rotation of the polygon's first point around the circle
 * @return the polygon
 */
public fun Circle.inscribed(sides: Int, rotation: Measure<Angle> = 0 * degrees): ConvexPolygon? {
    if (sides < 3) return null

    val interPointSweep = 360 / sides * degrees
    val topOfCircle     = center - Point(0.0, radius)

    val points = mutableListOf(Point(topOfCircle.x + radius * sin(rotation), topOfCircle.y + radius * (1 - cos(rotation))))

    var current: Point

    repeat(sides - 1) {
        val angle = interPointSweep * (it + 1) + rotation
        current = Point(topOfCircle.x + radius * sin(angle), topOfCircle.y + radius * (1 - cos(angle)))
        points += current
    }

    return ConvexPolygonImpl(points)
}

/**
 * Creates a [Star](https://math.stackexchange.com/questions/2135982/math-behind-creating-a-perfect-star) with n points
 * that is described by an outer and inner [Circle], which its concave and convex points respectively.
 *
 * @param circle to inscribe the polygon in
 * @param points the star should have
 * @param rotation of the star's first point around the circle
 * @param innerCircle defining the radius of the inner points
 * @return a star shaped polygon
 */
public fun star(circle     : Circle,
                points     : Int            = 5,
                rotation   : Measure<Angle> = 0 * degrees,
                innerCircle: Circle         = Circle(center = circle.center, radius = circle.radius * 2 / (3 + sqrt(5.0)))
): Polygon? = circle.inscribed(points, rotation)?.let { outerPoly ->
    innerCircle.inscribed(points, rotation + 360 / (2 * points) * degrees)?.let { innerPoly ->
        ConvexPolygonImpl(outerPoly.points.zip(innerPoly.points).flatMap { (f, s) -> listOf(f, s) })
    }
}

/**
 * Creates a rounded shape from a [Polygon]. The resulting shape is essentially a polygon with
 * the vertices rounded using a semi-circular curve.
 *
 * @see Polygon.rounded with config for control over radius at each point
 * @param radius for each point
 * @param filter deciding which points to apply the radius to
 * @return a [Path] for the new shape
 */
public fun Polygon.rounded(radius: Double, filter: (index: Int, Point) -> Boolean = { _,_ -> true }): Path = rounded { index, point ->
    when (filter(index, point)) {
        true -> radius
        else -> 0.0
    }
}

/**
 * Creates a rounded shape from a [Polygon]. The resulting shape is essentially a polygon with
 * the vertices rounded using a semi-circular curve.
 *
 * @param config determining the radius for each point in the polygon (with the given index)
 * @return a [Path] for the new shape
 */
public fun Polygon.rounded(config: (index: Int, Point) -> Double): Path {
    val newPoints = mutableListOf<PointRelationShip>()
    val radii     = mutableListOf<Double>()

    points.forEachIndexed { index, point ->
        radii += config(index, point)
    }

    points.forEachIndexed { index, point ->
        newPoints += when (val radius = radii[index]) {
            0.0  -> { PointRelationShip(point, point, point, 0.0, 0 * degrees, true) }
            else -> {
                val previousIndex = when (index) {
                    0    -> points.size - 1
                    else -> index - 1
                }

                val nextIndex = (index + 1) % points.size

                colinearPoint(points[previousIndex], point, points[nextIndex], radii[previousIndex], radius, radii[nextIndex])
            }
        }
    }

    val builder = path(newPoints[0].previous)

    newPoints.forEachIndexed { index, it ->
        if (index > 0) {
            builder.lineTo(it.previous)
        }

        val r = it.distance / (2 * cos(it.angle / 2))

        builder.arcTo(it.next, r, r, 0 * degrees, largeArch = false, sweep = it.isRight)
    }

    return builder.close()
}

private class PointRelationShip(
        val previous: Point,
        val point   : Point,
        val next    : Point,
        val distance: Double,
        val angle   : Measure<Angle>,
        val isRight : Boolean
)

internal val Vector2D.magnitude: Double get() =  sqrt(x*x + y*y)
internal operator fun Vector2D.times(other: Vector2D) = x * other.x + y * other.y

private fun colinearPoint(
        previous        : Point,
        point           : Point,
        next            : Point,
        previousDistance: Double,
        distance        : Double,
        nextDistance    : Double
): PointRelationShip {
    if (point == previous || point == next) return PointRelationShip(point, point, point, 0.0, 0 * degrees, true)

    val distancePrevious = point distanceFrom previous
    val distanceNext     = point distanceFrom next

    val left             = when {
        distance + previousDistance > distancePrevious -> distancePrevious / (distance + previousDistance) * distance
        else                                           -> distance
    }

    val right            = when {
        distance + nextDistance > distanceNext -> distanceNext / (distance + nextDistance) * distance
        else                                           -> distance
    }

    val radius = min(left, right)

    val scalePrevious = radius / distancePrevious
    val scaleNext     = radius / distanceNext

    val vector1 = point - previous
    val vector2 = next  - point

    // interior angle := cos(α) = a·b / (|a|·|b|)
    val angle = 180 * degrees - acos(vector1 * vector2 / (vector1.magnitude * vector2.magnitude)) * Angle.radians

    val direction = vector1.x * vector2.y - vector1.y * vector2.x

    val newPrevious = Identity.scale(around = point, x = scalePrevious, y = scalePrevious).invoke(previous).as2d()
    val newNext     = Identity.scale(around = point, x = scaleNext,     y = scaleNext    ).invoke(next    ).as2d()

    return PointRelationShip(
            previous = newPrevious,
            point    = point,
            next     = newNext,
            distance = newPrevious.distanceFrom(newNext),
            angle    = angle,
            isRight  = direction > 0
    )
}