package com.nectar.doodle.geometry


class Point(val x: Double = 0.0, val y: Double = 0.0) {

    operator fun plus (other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)

    operator fun times(value: Double) = Point(x * value, y * value)
    operator fun div  (value: Double) = Point(x / value, y / value)

    operator fun unaryMinus() = Point(-x, -y)

    override fun hashCode() = hashCode_

    override fun toString() = "[$x,$y]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { arrayOf(x, y).contentHashCode() }

    companion object {
        val Origin = Point()
    }
}