package io.nacular.doodle.geometry

import kotlin.math.pow
import kotlin.math.sqrt


/**
 * A two-dimensional position with an x and y component.
 *
 * @author Nicholas Eddy
 *
 * @property x x component
 * @property y y component
 *
 * @constructor
 * @param x x component
 * @param y y component
 */
open class Point(val x: Double = 0.0, val y: Double = 0.0) {

    constructor(x: Int   = 0,  y: Int   = 0 ): this(x.toDouble(), y.toDouble())
    constructor(x: Float = 0f, y: Float = 0f): this(x.toDouble(), y.toDouble())

    operator fun plus (other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)

    operator fun times(value: Int   ) = Point(x * value, y * value)
    operator fun div  (value: Int   ) = Point(x / value, y / value)
    operator fun times(value: Float ) = Point(x * value, y * value)
    operator fun div  (value: Float ) = Point(x / value, y / value)
    operator fun times(value: Double) = Point(x * value, y * value)
    operator fun div  (value: Double) = Point(x / value, y / value)

    operator fun unaryMinus() = Point(-x, -y)

    infix fun distanceFrom(other: Point) = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

    override fun hashCode() = hashCode_

    override fun toString() = "[$x,$y]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        return x == other.x && y == other.y
    }

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { arrayOf(x, y).contentHashCode() }

    companion object {
        /** Point at 0,0 */
        val Origin = Point(0, 0)
    }
}

operator fun Int.times   (value: Point) = value * this
operator fun Float.times (value: Point) = value * this
operator fun Double.times(value: Point) = value * this