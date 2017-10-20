package com.zinoti.jaz.geometry


class Point private constructor(val x: Double, val y: Double) {

    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    override fun hashCode(): Int = arrayOf(x, y).contentHashCode()

    override fun toString(): String = "[$x,$y]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    companion object {
        /** Creates a Point at (0,0).  */

        fun create(): Point = ORIGIN

        /**
         * Creates a Point at the given x and y location.
         *
         * @param aX The x
         * @param aY The y
         */

        fun create(aX: Double, aY: Double): Point = Point(aX, aY)

        val ORIGIN = Point(0.0, 0.0)
    }
}