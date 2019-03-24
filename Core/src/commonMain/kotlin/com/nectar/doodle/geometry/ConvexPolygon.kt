package com.nectar.doodle.geometry


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
    override val area: Double get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val empty: Boolean get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    private fun isLeft(line: Line, point: Point): Int = ((line.end.x - line.start.x) * (point.y - line.start.y) - (point.x - line.start.x) * (line.end.y - line.start.y)).toInt()

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

    companion object {
        operator fun invoke(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon {
            return ConvexPolygonImpl(first, second, third, *remaining)
        }
    }
}

private class ConvexPolygonImpl(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon() {
    override val points = listOf(first, second, third) + remaining
}