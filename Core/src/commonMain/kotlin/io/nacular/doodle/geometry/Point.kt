package io.nacular.doodle.geometry

import kotlin.math.pow
import kotlin.math.sqrt


/**
 * A two-dimensional position with an x and y component.
 *
 * @author Nicholas Eddy
 *
 * @constructor creates a new Point
 * @property x position
 * @property y position
 */
open class Point(val x: Double = 0.0, val y: Double = 0.0) {

    /**
     * Creates a new Point.
     *
     * @param x position
     * @param y position
     */
    constructor(x: Int = 0, y: Int = 0 ): this(x.toDouble(), y.toDouble())

    /**
     * Creates a new Point.
     *
     * @param x position
     * @param y position
     */
    constructor(x: Float = 0f, y: Float = 0f): this(x.toDouble(), y.toDouble())

    /**
     * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two Points.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    /**
     * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two Points.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this Point and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    operator fun times(value: Int   ) = Point(x * value, y * value)
    operator fun times(value: Float ) = Point(x * value, y * value)
    operator fun times(value: Double) = Point(x * value, y * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this Point
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    operator fun div(value: Int   ) = Point(x / value, y / value)
    operator fun div(value: Float ) = Point(x / value, y / value)
    operator fun div(value: Double) = Point(x / value, y / value)

    /**
     * Performs a negation of this point, resulting in a new point with inverted x and y directions.
     */
    operator fun unaryMinus() = Point(-x, -y)

    /**
     * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two points.
     *
     * @param other point to compare
     * @return distance between the points
     */
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

/** @see Point.times */
operator fun Int.times   (value: Point) = value * this

/** @see Point.times */
operator fun Float.times (value: Point) = value * this

/** @see Point.times */
operator fun Double.times(value: Point) = value * this