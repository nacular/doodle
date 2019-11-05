package com.nectar.doodle.geometry

import kotlin.math.abs


abstract class Polygon: Shape {
    abstract val points: List<Point>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polygon) return false

        if (points != other.points) return false

        return true
    }

    override fun hashCode() = points.hashCode()

    override val boundingRectangle: Rectangle by lazy {
        val minX = points.minBy { it.x }!!.x
        val minY = points.minBy { it.y }!!.y
        val maxX = points.maxBy { it.x }!!.x
        val maxY = points.maxBy { it.y }!!.y

        Rectangle(Point(minX, minY), Size(maxX - minX, maxY - minY))
    }

    override fun contains  (rectangle: Rectangle) = rectangle.position in this && Point(rectangle.right, rectangle.bottom) in this
    override fun intersects(rectangle: Rectangle): Boolean { TODO("not implemented") }
}

private data class Line(val start: Point, val end: Point)

abstract class ConvexPolygon: Polygon() {
    // https://en.wikipedia.org/wiki/Shoelace_formula
    override val area: Double by lazy {
        var area = 0.0         // Accumulates area in the loop
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
     */
    override fun contains(point: Point): Boolean {
        var result = 0 // the winding number counter

        points.forEachIndexed { i, vertex ->
            when {
                vertex.y <= point.y ->
                    if (points[i+1].y > point.y && isLeft(Line(vertex, points[i+1]), point) > 0) {
                        ++result
                    }
                    // have  a valid up intersect
                else ->
                    if (points[i+1].y <= point.y && isLeft(Line(vertex, points[i+1]), point) < 0) {
                        --result
                    }
                    // have  a valid down intersect
            }
        }

        return result == 0
    }

    private fun isLeft(line: Line, point: Point): Int = ((line.end.x - line.start.x) * (point.y - line.start.y) - (point.x - line.start.x) * (line.end.y - line.start.y)).toInt()

    companion object {
        operator fun invoke(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon {
            return ConvexPolygonImpl(first, second, third, *remaining)
        }
    }
}

private class ConvexPolygonImpl(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon() {
    override val points = listOf(first, second, third) + remaining
}