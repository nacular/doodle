package io.nacular.doodle.geometry

import io.nacular.doodle.utils.lerp
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.radians
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

public typealias Vector2D = Point

/**
 * A two-dimensional vector with an x and y component.
 *
 * @author Nicholas Eddy
 */
public sealed interface Point: Vector3D {

    /**
     * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    public operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)

    /**
     * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other point being added to this
     * @return the resulting point
     */
    public operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public override operator fun times(value: Int): Vector2D

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public override operator fun times(value: Float): Vector2D

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public override operator fun times(value: Double): Vector2D

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public override operator fun div(value: Int): Vector2D

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public override operator fun div(value: Float): Vector2D

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public override operator fun div(value: Double): Vector2D

    /**
     * Performs a negation of this vector, resulting in a new vector with inverted x and y directions.
     */
    public override operator fun unaryMinus(): Vector2D

    /**
     * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two points.
     *
     * @param other vector to compare
     * @return distance between the vectors
     */
    public infix fun distanceFrom(other: Vector2D): Double = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

    public fun as3d(): Vector3D

    public override fun magnitude(): Double = sqrt(x*x + y*y)

    public companion object {
        /** Vector at 0,0 */
        public val Origin: Vector2D = Vector2D(0, 0)

        /**
         * Creates a new Vector2d.
         *
         * @param x position
         * @param y position
         */
        public operator fun invoke(x: Number = 0.0, y: Number = 0.0): Vector2D = VectorImpl(x.toDouble(), y.toDouble())
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
public sealed interface Vector3D {
    /**
     * x position
     */
    public val x: Double

    /**
     * y position
     */
    public val y: Double

    /**
     * z position
     */
    public val z: Double

    /**
     * Calculates the [vector sum](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other vector being added to this
     * @return the resulting point
     */
    public operator fun plus(other: Vector3D): Vector3D = Vector3D(x + other.x, y + other.y, z + other.z)

    /**
     * Calculates the [vector difference](https://en.wikipedia.org/wiki/Euclidean_vector#Addition_and_subtraction) of two vectors.
     *
     * @param other vector being added to this
     * @return the resulting point
     */
    public operator fun minus(other: Vector3D): Vector3D = Vector3D(x - other.x, y - other.y, z - other.z)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun times(value: Int): Vector3D = Vector3D(x * value, y * value, z * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun times(value: Float): Vector3D = Vector3D(x * value, y * value, z * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector and a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun times(value: Double): Vector3D = Vector3D(x * value, y * value, z * value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun div(value: Int): Vector3D = Vector3D(x / value, y / value, z / value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun div(value: Float): Vector3D = Vector3D(x / value, y / value, z / value)

    /**
     * Calculates the [scalar product](https://en.wikipedia.org/wiki/Scalar_multiplication) of this vector
     * and the inverse of a value.
     *
     * @param value to scale by
     * @return the resulting point
     */
    public operator fun div(value: Double): Vector3D = Vector3D(x / value, y / value, z / value)

    /**
     * Performs a negation of this vector, resulting in a new vector with inverted x and y directions.
     */
    public operator fun unaryMinus(): Vector3D = Vector3D(-x, -y, -z)

    /**
     * Calculates the [euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) between two vectors.
     *
     * @param other vector to compare
     * @return distance between the vectors
     */
    public infix fun distanceFrom(other: Vector3D): Double = sqrt((x - other.x).pow(2) + (y - other.y).pow(2) + (z - other.z).pow(2))

    /**
     * Gets a 2D version of this vector
     */
    public fun as2d(): Vector2D

    /**
     * Computes the [cross-product](https://en.wikipedia.org/wiki/Cross_product) of the two vectors.
     *
     * @param other vector
     */
    public infix fun cross(other: Vector3D): Vector3D

    /**
     * Computes the [dot-product](https://en.wikipedia.org/wiki/Dot_product) of the two vectors
     *
     * @param other vector
     */
    public operator fun times(other: Vector3D): Double  = x * other.x + y * other.y + z * other.z

    /**
     *  Gets a unit vector pointing in the same direction as this one, if (and only if) the vector's length
     *  is not 0. Otherwise, the vector itself is returned.
     *
     *  @return a unit vector pointing in the same direction as this one IFF this vector has a non-zero length
     */
    public fun normalize(): Vector3D

    /**
     * Gets the vector's length
     */
    public fun magnitude(): Double

    public companion object {

        /**
         * Creates a new Vector3d.
         *
         * @param x position
         * @param y position
         * @param z position
         */
        public operator fun invoke(x: Number = 0, y: Number = 0, z: Number = 0): Vector3D = VectorImpl(x.toDouble(), y.toDouble(), z.toDouble())
    }
}

/** @see Point.times */
public operator fun Int.times(value: Vector2D): Vector2D = value * this

/** @see Point.times */
public operator fun Float.times(value: Vector2D): Vector2D = value * this

/** @see Point.times */
public operator fun Double.times(value: Vector2D): Vector2D = value * this

/** @see Vector3D.times */
public operator fun Int.times(value: Vector3D): Vector3D = value * this

/** @see Vector3D.times */
public operator fun Float.times(value: Vector3D): Vector3D = value * this

/** @see Vector3D.times */
public operator fun Double.times(value: Vector3D): Vector3D = value * this

/**
 * Interpolates between 2 points
 */
public fun lerp(first: Vector2D, second: Vector2D, fraction: Float): Vector2D = Point(lerp(first.x, second.x, fraction), lerp(first.y, second.y, fraction))

/**
 * Calculate the interior angle between 2 vectors => `angle := cos(α) = a·b / (|a|·|b|)`.
 *
 * @param other vector to compare [this] with
 */
public infix fun Vector3D.interiorAngle(other: Vector3D): Measure<Angle> = 180 * degrees - acos(this * other / (magnitude() * other.magnitude())) * radians

internal class VectorImpl(override val x: Double = 0.0, override val y: Double = 0.0, override val z: Double = 0.0): Vector2D, Vector3D {
    override inline fun as2d() = this
    override inline fun as3d() = this

    private val magnitude: Double by lazy { sqrt(x.pow(2) + y.pow(2) + z.pow(2)) }

    override fun toString(): String = "[$x,$y,$z]"

    /**
     * Computes the [cross-product](https://en.wikipedia.org/wiki/Cross_product) of the two vectors.
     *
     * @param other vector
     */
    override infix fun cross(other: Vector3D): Vector3D {
        val aX = x
        val aY = y
        val aZ = z
        val bX = other.x
        val bY = other.y
        val bZ = other.z

        return VectorImpl(
            x =   aY * bZ - aZ * bY,
            y = -(aX * bZ - aZ * bX),
            z =   aX * bY - aY * bX
        )
    }

    override fun times(value: Int   ): Vector2D = VectorImpl(x * value, y * value, z * value)
    override fun times(value: Float ): Vector2D = VectorImpl(x * value, y * value, z * value)
    override fun times(value: Double): Vector2D = VectorImpl(x * value, y * value, z * value)
    override fun div  (value: Int   ): Vector2D = VectorImpl(x / value, y / value, z / value)
    override fun div  (value: Float ): Vector2D = VectorImpl(x / value, y / value, z / value)
    override fun div  (value: Double): Vector2D = VectorImpl(x / value, y / value, z / value)

    override fun unaryMinus(): Vector2D = VectorImpl(-x, -y, -z)

    /**
     * Computes the [dot-product](https://en.wikipedia.org/wiki/Dot_product) of the two vectors
     *
     * @param other vector
     */
    override operator fun times(other: Vector3D): Double  = x * other.x + y * other.y + z * other.z

    /**
     *  Gets a unit vector pointing in the same direction as this one, if (and only if) the vector's length
     *  is not 0. Otherwise, the vector itself is returned.
     *
     *  @return a unit vector pointing in the same direction as this one IFF this vector has a non-zero length
     */
    override fun normalize(): Vector3D = when (magnitude) {
        0.0, 1.0 -> this
        else     -> VectorImpl(x / magnitude, y / magnitude, z / magnitude)
    }

    override fun magnitude() = magnitude

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vector3D) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return z == other.z
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }
}

internal class Ray(val position: Vector3D, direction: Vector3D) {
    val direction = direction.normalize()
}

internal class Plane(val position: Vector3D, direction: Vector3D) {
    val direction = direction.normalize()

    infix fun intersection(with: Ray): Vector3D? {
        val denominator = direction * with.direction

        if (denominator != 0.0) {
            val p0l0 = position - with.position
            val t    = p0l0 * direction / denominator

            return with.position + t * with.direction
        }

        return null
    }
}