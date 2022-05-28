package io.nacular.doodle.geometry

import kotlin.math.pow
import kotlin.math.sqrt

public typealias Vector2d = Point

/**
 * A two-dimensional vector with an x and y component.
 *
 * @author Nicholas Eddy
 */
public interface Point {
    /**
     * x position
     */
    public val x: Double

    /**
     * y position
     */
    public val y: Double

    /**
     * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    public operator fun plus (other: Vector2d): Vector2d = Vector2d(x + other.x, y + other.y)

    /**
     * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other vector being added to this
     * @return the resulting point
     */
    public operator fun plus(other: Vector3d): Vector3d = Vector3d(x + other.x, y + other.y, 0 + other.z)

    /**
     * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    public operator fun minus(other: Vector2d): Vector2d = Vector2d(x - other.x, y - other.y)

    /**
     * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other vector being added to this
     * @return the resulting point
     */
    public operator fun minus(other: Vector3d): Vector3d = Vector3d(x - other.x, y - other.y, 0 - other.z)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun times(value: Int): Vector2d = Vector2d(x * value, y * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun times(value: Float): Vector2d = Vector2d(x * value, y * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun times(value: Double): Vector2d = Vector2d(x * value, y * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun div(value: Int): Vector2d = Vector2d(x / value, y / value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun div(value: Float): Vector2d = Vector2d(x / value, y / value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun div(value: Double): Vector2d = Vector2d(x / value, y / value)

    /**
     * Performs a negation of this vector, resulting in a new vector with inverted x and y directions.
     */
    public operator fun unaryMinus(): Vector2d = Vector2d(-x, -y)

    /**
     * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two points.
     *
     * @param other vector to compare
     * @return distance between the vectors
     */
    public infix fun distanceFrom(other: Vector2d): Double = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

    /**
     * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two vectors.
     *
     * @param other vector to compare
     * @return distance between the vectors
     */
    public infix fun distanceFrom(other: Vector3d): Double = sqrt((x - other.x).pow(2) + (y - other.y).pow(2) + (0 - other.z).pow(2))

    public fun as3d(): Vector3d

    public companion object {
        /** Vector at 0,0 */
        public val Origin: Vector2d = Vector2d(0, 0)

        /**
         * Creates a new Vector2d.
         *
         * @param x position
         * @param y position
         */
        public operator fun invoke(x: Number = 0.0, y: Number = 0.0): Vector2d = VectorImpl(x.toDouble(), y.toDouble())
    }
}

/**
 * A three-dimensional vector with an x, y and z component.
 *
 * @author Nicholas Eddy
 * @property x position
 * @property y position
 * @property z position
 */
public abstract class Vector3d protected constructor(public val x: Double = 0.0, public val y: Double = 0.0, public val z: Double = 0.0) {
    override fun toString(): String = "[$x,$y,$z]"

    /**
     * Gets a 2D version of this vector
     */
    public abstract fun as2d(): Vector2d

    /**
     * Computes the [cross-product](https://en.wikipedia.org/wiki/Cross_product) of the two vectors.
     *
     * @param other vector
     */
    public infix fun cross(other: Vector3d): Vector3d {
        val a1 = x
        val a2 = y
        val a3 = z
        val b1 = other.x
        val b2 = other.y
        val b3 = other.z

        return VectorImpl(
            x =   a2 * b3 - a3 * b2,
            y = -(a1 * b3 - a3 * b1),
            z =   a1 * b2 - a2 * b1
        )
    }

    /**
     * Computes the [dot-product](https://en.wikipedia.org/wiki/Dot_product) of the two vectors
     *
     * @param other vector
     */
    public operator fun times(other: Vector3d): Double  = x * other.x + y * other.y + z * other.z

    /**
     *  Gets a unit vector pointing in the same direction as this one, if (and only if) the vector's length
     *  is not 0. Otherwise, the vector itself is returned.
     *
     *  @return a unit vector pointing in the same direction as this one IFF this vector has a non-zero length
     */
    public fun normalize(): Vector3d = when (magnitude) {
        0.0  -> this
        else -> VectorImpl(x / magnitude, y / magnitude, z / magnitude)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vector3d) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    private val magnitude: Double by lazy { sqrt(x.pow(2) + y.pow(2) + z.pow(2)) }

    public companion object {

        /**
         * Creates a new Vector3d.
         *
         * @param x position
         * @param y position
         * @param z position
         */
        public operator fun invoke(x: Number = 0, y: Number = 0, z: Number = 0): Vector3d = VectorImpl(x.toDouble(), y.toDouble(), z.toDouble())
    }
}

/**
 * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
 *
 * @param other vector being added to this
 * @return the resulting point
 */
public operator fun Vector3d.plus(other: Vector2d): Vector3d = Vector3d(x + other.x, y + other.y, z)

/**
 * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
 *
 * @param other vector being added to this
 * @return the resulting point
 */
public operator fun Vector3d.plus(other: Vector3d): Vector3d = Vector3d(x + other.x, y + other.y, z + other.z)

/**
 * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
 *
 * @param other vector being added to this
 * @return the resulting point
 */
public operator fun Vector3d.minus(other: Vector2d): Vector3d = Vector3d(x - other.x, y - other.y, z)

/**
 * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
 *
 * @param other vector being added to this
 * @return the resulting point
 */
public operator fun Vector3d.minus(other: Vector3d): Vector3d = Vector3d(x - other.x, y - other.y, z - other.z)

/**
 * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
 * and a value.
 *
 * @param value to scale by
 * @return the resulting point
 */
public operator fun Vector3d.times(value: Int   ): Vector3d = Vector3d(x * value, y * value, z * value)

/**
 * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
 * and a value.
 *
 * @param value to scale by
 * @return the resulting point
 */
public operator fun Vector3d.times(value: Float ): Vector3d = Vector3d(x * value, y * value, z * value)

/**
 * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
 * and a value.
 *
 * @param value to scale by
 * @return the resulting point
 */
public operator fun Vector3d.times(value: Double): Vector3d = Vector3d(x * value, y * value, z * value)

/**
 * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
 * and the inverse of a value.
 *
 * @param value to scale by
 * @return the resulting point
 */
public operator fun Vector3d.div(value: Int   ): Vector3d = Vector3d(x / value, y / value, z / value)
public operator fun Vector3d.div(value: Float ): Vector3d = Vector3d(x / value, y / value, z / value)
public operator fun Vector3d.div(value: Double): Vector3d = Vector3d(x / value, y / value, z / value)

/**
 * Performs a negation of this vector, resulting in a new vector with inverted x, y and z directions.
 */
public operator fun Vector3d.unaryMinus(): Vector3d = Vector3d(-x, -y, -z)

/**
 * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two vectors.
 *
 * @param other vector to compare
 * @return distance between the vectors
 */
public infix fun Vector3d.distanceFrom(other: Vector2d): Double = other distanceFrom this

/**
 * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two vectors.
 *
 * @param other vector to compare
 * @return distance between the vectors
 */
public infix fun Vector3d.distanceFrom(other: Vector3d): Double = sqrt((x - other.x).pow(2) + (y - other.y).pow(2) + (z - other.z).pow(2))

/** @see Point.times */
public operator fun Int.times(value: Vector2d): Vector2d = value * this

/** @see Point.times */
public operator fun Float.times(value: Vector2d): Vector2d = value * this

/** @see Point.times */
public operator fun Double.times(value: Vector2d): Vector2d = value * this

/** @see Vector3d.times */
public operator fun Int.times(value: Vector3d): Vector3d = value * this

/** @see Vector3d.times */
public operator fun Float.times(value: Vector3d): Vector3d = value * this

/** @see Vector3d.times */
public operator fun Double.times(value: Vector3d): Vector3d = value * this

internal class VectorImpl(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Vector2d, Vector3d(x, y, z) {
    override inline fun as2d() = this
    override inline fun as3d() = this
}

internal class Ray(val position: Vector3d, direction: Vector3d) {
    val direction = direction.normalize()
}

internal class Plane(val position: Vector3d, direction: Vector3d) {
    val direction = direction.normalize()

    infix fun intersection(with: Ray): Vector3d? {
        val denominator = direction * with.direction

        if (denominator != 0.0) {
            val p0l0 = position - with.position
            val t    = p0l0 * direction / denominator

            return with.position + t * with.direction
        }

        return null
    }
}