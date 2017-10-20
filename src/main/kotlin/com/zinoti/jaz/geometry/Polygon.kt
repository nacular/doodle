package com.zinoti.jaz.geometry


class Polygon protected constructor(first: Point, second: Point, third: Point, vararg points: Point): Shape {
    val points: List<Point> = mutableListOf(first) + second + third + points

    override val area             : Double    get() = TODO("not implemented")
    override val empty            : Boolean   get() = TODO("not implemented")
    override val boundingRectangle: Rectangle get() = TODO("not implemented")

    override fun contains  (point    : Point    ): Boolean { TODO("not implemented") }
    override fun contains  (rectangle: Rectangle): Boolean { TODO("not implemented") }
    override fun intersects(rectangle: Rectangle): Boolean { TODO("not implemented") }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polygon) return false

        if (points != other.points) return false

        return true
    }

    override fun hashCode() = points.hashCode()

    companion object {
        /**
         * Creates a Polygon with vertices at the given points.
         *
         * @param aPoints The points
         */

        fun create(first: Point, second: Point, third: Point, vararg points: Point) = Polygon(first, second, third, *points)
    }
}