package com.nectar.doodle.geometry


private class ConvexPolygonImpl(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon() {
    override val points = listOf(first, second, third) + remaining
    override val area : Double  get() = TODO("not implemented")
    override val empty: Boolean get() = TODO("not implemented")

    override fun contains  (point    : Point    ): Boolean { TODO("not implemented") }
    override fun contains  (rectangle: Rectangle): Boolean { TODO("not implemented") }
    override fun intersects(rectangle: Rectangle): Boolean { TODO("not implemented") }
}

abstract class ConvexPolygon protected constructor(): Shape {
    abstract val points: List<Point>

    override val boundingRectangle: Rectangle by lazy {
        val minX = points.minBy { it.x }!!.x
        val minY = points.minBy { it.y }!!.y
        val maxX = points.maxBy { it.x }!!.x
        val maxY = points.maxBy { it.y }!!.y

        Rectangle(Point(minX, minY), Size(maxX - minX, maxY - minY))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConvexPolygon) return false

        if (points != other.points) return false

        return true
    }

    override fun hashCode() = points.hashCode()

    companion object {
        operator fun invoke(first: Point, second: Point, third: Point, vararg remaining: Point): ConvexPolygon {
            return ConvexPolygonImpl(first, second, third, *remaining)
        }
    }
}