package com.nectar.doodle.geometry

import com.nectar.measured.units.Angle
import com.nectar.measured.units.Angle.Companion.cos
import com.nectar.measured.units.Angle.Companion.degrees
import com.nectar.measured.units.Angle.Companion.sin
import com.nectar.measured.units.Measure
import com.nectar.measured.units.times
import kotlin.math.abs
import kotlin.math.sqrt


/**
 * A [Shape] defined by a set of line segments that connect to enclose a region.
 */
abstract class Polygon: Shape {

    /** Points representing the verticies */
    abstract val points: List<Point>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polygon) return false

        if (points != other.points) return false

        return true
    }

    override fun hashCode() = points.hashCode()

    /**
     * Gives the smallest [Rectangle] that fully contains this Polygon.
     *
     * ```
     * +---------------------+
     * |     **********      |
     * |   ************      |
     * | **************      |
     * |*********************|
     * |*********************|
     * |******************** |
     * |*******************  |
     * |       ************  |
     * |        ***********  |
     * |         **********  |
     * +---------------------+
     *
     * ```
     */
    override val boundingRectangle: Rectangle by lazy {
        val minX = points.minBy { it.x }!!.x
        val minY = points.minBy { it.y }!!.y
        val maxX = points.maxBy { it.x }!!.x
        val maxY = points.maxBy { it.y }!!.y

        Rectangle(Point(minX, minY), Size(maxX - minX, maxY - minY))
    }
}

abstract class ConvexPolygon: Polygon() {
    private data class Line(val start: Point, val end: Point)

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
    override fun contains(rectangle: Rectangle) = rectangle.points.all { contains(it) }

    override fun intersects(rectangle: Rectangle): Boolean = TODO("not implemented")

    private fun isLeft(line: Line, point: Point): Int = ((line.end.x - line.start.x) * (point.y - line.start.y) - (point.x - line.start.x) * (line.end.y - line.start.y)).toInt()

    companion object {
        operator fun invoke(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon {
            return ConvexPolygonImpl(first, second, third, *remaining)
        }
    }
}

private class ConvexPolygonImpl(override val points: List<Point>): ConvexPolygon() {
    constructor(first: Point, second: Point, third: Point, vararg remaining: Point): this(listOf(first, second, third) + remaining)
}

fun inscribed(circle: Circle, sides: Int, rotation: Measure<Angle> = 0 * degrees): ConvexPolygon? {
    if (sides < 3) return null

    val interPointSweep = 360 / sides * degrees
    val topOfCircle     = circle.center - Point(0.0, circle.radius)

    val points = mutableListOf(Point(topOfCircle.x + circle.radius * sin(rotation), topOfCircle.y + circle.radius * (1 - cos(rotation))))

    var current: Point

    repeat(sides - 1) {
        val angle = interPointSweep * (it + 1) + rotation
        current = Point(topOfCircle.x + circle.radius * sin(angle), topOfCircle.y + circle.radius * (1 - cos(angle)))
        points += current
    }

    return ConvexPolygonImpl(points)
}

/**
 * https://math.stackexchange.com/questions/2135982/math-behind-creating-a-perfect-star
 */
fun star(circle: Circle, points: Int = 5, rotation: Measure<Angle> = 0 * degrees, innerCircle: Circle = Circle(center = circle.center, radius = circle.radius * 2 / (3 + sqrt(5.0)))): Polygon? {
    return inscribed(circle, points, rotation)?.let { outerPoly ->
        inscribed(innerCircle, points, rotation + 360 / (2 * points) * degrees)?.let { innerPoly ->
            ConvexPolygonImpl(outerPoly.points.zip(innerPoly.points).flatMap { (f, s) -> listOf(f, s) })
        }
    }
}