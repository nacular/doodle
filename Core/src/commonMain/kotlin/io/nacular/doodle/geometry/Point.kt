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
public open class Point(public val x: Double = 0.0, public val y: Double = 0.0) {

    /**
     * Creates a new Point.
     *
     * @param x position
     * @param y position
     */
    public constructor(x: Int = 0, y: Int = 0 ): this(x.toDouble(), y.toDouble())

    /**
     * Creates a new Point.
     *
     * @param x position
     * @param y position
     */
    public constructor(x: Float = 0f, y: Float = 0f): this(x.toDouble(), y.toDouble())

    /**
     * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two Points.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    public operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)

    /**
     * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two Points.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    public operator fun minus(other: Point): Point = Point(x - other.x, y - other.y)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this Point and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun times(value: Int   ): Point = Point(x * value, y * value)
    public operator fun times(value: Float ): Point = Point(x * value, y * value)
    public operator fun times(value: Double): Point = Point(x * value, y * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this Point
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun div(value: Int   ): Point = Point(x / value, y / value)
    public operator fun div(value: Float ): Point = Point(x / value, y / value)
    public operator fun div(value: Double): Point = Point(x / value, y / value)

    /**
     * Performs a negation of this point, resulting in a new point with inverted x and y directions.
     */
    public operator fun unaryMinus(): Point = Point(-x, -y)

    /**
     * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two points.
     *
     * @param other point to compare
     * @return distance between the points
     */
    public infix fun distanceFrom(other: Point): Double = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

    override fun hashCode(): Int = hashCode_

    override fun toString(): String = "[$x,$y]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        return x == other.x && y == other.y
    }

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { arrayOf(x, y).contentHashCode() }

    public companion object {
        /** Point at 0,0 */
        public val Origin: Point = Point(0, 0)
    }
}

/** @see Point.times */
public operator fun Int.times(value: Point): Point = value * this

/** @see Point.times */
public operator fun Float.times(value: Point): Point = value * this

/** @see Point.times */
public operator fun Double.times(value: Point): Point = value * this