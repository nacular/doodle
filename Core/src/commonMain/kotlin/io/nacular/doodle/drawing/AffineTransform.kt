package io.nacular.doodle.drawing

import io.nacular.doodle.core.Internal
import io.nacular.doodle.drawing.AffineTransform.Companion.invoke
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Vector3d
import io.nacular.doodle.geometry.times
import io.nacular.doodle.geometry.unaryMinus
import io.nacular.doodle.utils.AffineMatrix3D
import io.nacular.doodle.utils.div
import io.nacular.doodle.utils.minus
import io.nacular.doodle.utils.plus
import io.nacular.doodle.utils.times
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.acos
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.jvm.JvmName
import kotlin.math.atan
import kotlin.math.sqrt

/**
 * Represents an [Affine Transformation](https://en.wikipedia.org/wiki/Affine_transformation).
 *
 * @see AffineMatrix3D to see the underlying matrix used for such a transform.
 */
@Suppress("ReplaceSingleLineLet")
public class AffineTransform internal constructor(internal val matrix: AffineMatrix3D) {
    /**
     * Creates a transform with the given properties.
     *
     * @param scaleX     how much to scale the x direction
     * @param shearX     how much to shear the x direction
     * @param translateX how much to translate in the x direction
     * @param scaleY     how much to scale the y direction
     * @param shearY     how much to shear the y direction
     * @param translateY how much to translate in the y direction
     */
    public constructor(
            scaleX    : Double = 1.0,
            shearX    : Double = 0.0,
            translateX: Double = 0.0,
            scaleY    : Double = 1.0,
            shearY    : Double = 0.0,
            translateY: Double = 0.0):
            this(AffineMatrix3D(scaleX, shearX, translateX,
                                shearY, scaleY, translateY))

    /** `true` if this transform is equal to the [Identity] transform */
    public val isIdentity: Boolean = matrix.isIdentity

    /** Scale component in the x direction */
    public val scaleX: Double get() = m00

    /** Shear component in the x direction */
    public inline val shearX: Double get() = shearXY

    /** Shear component in the x direction with z unchanged */
    public val shearXY: Double get() = m01

    /** Translation component in the x direction */
    public val translateX: Double get() = if (is3d) m03 else m02

    /** Shear component in the y direction */
    public inline val shearY: Double get() = shearYX

    /** Shear component in the y direction with z unchanged */
    public val shearYX: Double get() = m10

    /** Scale component in the y direction */
    public val scaleY: Double get() = m11

    /** Translation component in the y direction */
    public val translateY: Double get() = if (is3d) m13 else m12

    /** Scale component in the z direction */
    public val scaleZ: Double get() = m20

    /** Shear component in the x direction with y unchanged */
    public val shearXZ: Double get() = m02

    /** Shear component in the y direction with y unchanged */
    public val shearYZ: Double get() = m12

    /** Shear component in the z direction with x unchanged */
    public val shearZX: Double get() = m10

    /** Shear component in the z direction with y unchanged */
    public val shearZY: Double get() = m11

    /** Translation component in the z direction */
    public val translateZ: Double get() = if (is3d) m03 else m22

    @Internal public val m00: Double get() = matrix[0, 0]
    @Internal public val m01: Double get() = matrix[0, 1]
    @Internal public val m02: Double get() = matrix[0, 2]
    @Internal public val m03: Double get() = matrix[0, 3]
    @Internal public val m10: Double get() = matrix[1, 0]
    @Internal public val m11: Double get() = matrix[1, 1]
    @Internal public val m12: Double get() = matrix[1, 2]
    @Internal public val m13: Double get() = matrix[1, 3]
    @Internal public val m20: Double get() = matrix[2, 0]
    @Internal public val m21: Double get() = matrix[2, 1]
    @Internal public val m22: Double get() = matrix[2, 2]
    @Internal public val m23: Double get() = matrix[2, 3]

    @Internal public val is3d: Boolean = !isIdentity && matrix.numColumns > 3

    internal fun as3d(): AffineTransform = when {
        is3d -> this
        else -> AffineTransform(
            matrix.get(0,0,true),
            matrix.get(0,1,true),
            matrix.get(0,2,true),
            matrix.get(0,3,true),
            matrix.get(1,0,true),
            matrix.get(1,1,true),
            matrix.get(1,2,true),
            matrix.get(1,3,true),
            matrix.get(2,0,true),
            matrix.get(2,1,true),
            matrix.get(2,2,true),
            matrix.get(2,3,true)
        )
    }

    /**
     * Allows transforms to be combined sequentially.
     *
     * ```
     *
     * val transformC = transformA * transformB
     *
     * ```
     *  So applying `transformC` is equivalent to applying `transformA` then `transformB`.
     *
     * @see times
     */
    public operator fun times(other: AffineTransform): AffineTransform = AffineTransform(matrix * other.matrix)

    /**
     * Append a scale operation (around the [Origin][io.nacular.doodle.geometry.Point.Origin]) to this transform.
     *
     * @param x amount to scale in the x-direction
     * @param y amount to scale in the y-direction
     * @param z amount to scale in the z-direction
     * @return a new transform
     */
    public fun scale(x: Double = 1.0, y: Double = 1.0, z: Double = 1.0): AffineTransform = AffineTransform(
            matrix * when (z) {
                1.0 -> AffineMatrix3D(
                    x,   0.0, 0.0,
                    0.0,   y, 0.0)
                else -> AffineMatrix3D(
                      x, 0.0, 0.0, 0.0,
                    0.0,   y, 0.0, 0.0,
                    0.0, 0.0,   z, 0.0)
            }
    )

    /**
     * Append a scale operation to this transform, that scales around the given point.
     *
     * @param around this point
     * @param x amount to scale in the x-direction
     * @param y amount to scale in the y-direction
     * @param z amount to scale in the z-direction
     * @return a new transform
     */
    public inline fun scale(around: Point, x: Double = 1.0, y: Double = 1.0, z: Double = 1.0): AffineTransform = scale(around.as3d(), x, y, z)

    public fun scale(around: Vector3d, x: Double = 1.0, y: Double = 1.0, z: Double = 1.0): AffineTransform = (this translate around).scale(x, y, z) translate -around

    /**
     * Append a translation operation to this transform.
     *
     * @param by the x and y components of this point
     * @see translate
     * @return a new transform
     */
    public inline infix fun translate(by: Point): AffineTransform = translate(by.x, by.y, 0.0)

    /**
     * Append a translation operation to this transform.
     *
     * @param by the x, y and z components of this vector
     * @see translate
     * @return a new transform
     */
    public infix fun translate(by: Vector3d): AffineTransform = translate(by.x, by.y, by.z)

    /**
     * Append a translation operation to this transform.
     *
     * @param x component
     * @param y component
     * @param z component
     * @see translate
     * @return a new transform
     */
    public fun translate(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): AffineTransform = AffineTransform(
            matrix * when (z) {
                0.0  -> AffineMatrix3D(
                    1.0, 0.0, x,
                    0.0, 1.0, y)
                else -> AffineMatrix3D(
                    1.0, 0.0, 0.0, x,
                    0.0, 1.0, 0.0, y,
                    0.0, 0.0, 1.0, z)
            }
    )

    /**
     * Append a skew operation to this transform.
     *
     * @param x component
     * @param y component
     * @param z component
     * @return a new transform
     */
    public fun skew(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): AffineTransform = AffineTransform(
            matrix * when (z) {
                0.0 -> AffineMatrix3D(
                    1.0,   x, 0.0,
                    y,   1.0, 0.0)
                else -> AffineMatrix3D(
                    1.0, 0.0,   x, 0.0,
                    0.0,   y, 1.0, 0.0,
                      z, 0.0, 1.0, 0.0)
            }
    )

    /**
     * Appends a rotation operation (around the z-axis), at the [Origin][io.nacular.doodle.geometry.Point.Origin] to this transform.
     *
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public inline infix fun rotate(by: Measure<Angle>): AffineTransform = rotateZ(by)

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public inline fun rotate(around: Point, by: Measure<Angle>): AffineTransform = rotateZ(around, by)

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public inline fun rotate(around: Vector3d, by: Measure<Angle>): AffineTransform = rotateZ(around, by)

    /**
     * Appends a rotation operation (around the z-axis), at the [Origin][io.nacular.doodle.geometry.Point.Origin] to this transform.
     *
     * @param by this angle
     * @return a new transform
     */
    public infix fun rotateZ(by: Measure<Angle>): AffineTransform {
        val sin = sin(by)
        val cos = cos(by)

        return AffineTransform(
            matrix * AffineMatrix3D(
                cos, -sin, 0.0,
                sin,  cos, 0.0)
        )
    }

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public inline fun rotateZ(around: Point, by: Measure<Angle>): AffineTransform = rotateZ(around.as3d(), by)

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public fun rotateZ(around: Vector3d, by: Measure<Angle>): AffineTransform = this translate around rotateZ by translate -around

    /**
     * Appends a rotation operation (around the x-axis), at the [Origin][io.nacular.doodle.geometry.Point.Origin] to this transform.
     *
     * @param by this angle
     * @return a new transform
     */
    public infix fun rotateX(by: Measure<Angle>): AffineTransform {
        val sin = sin(by)
        val cos = cos(by)

        return AffineTransform(
            matrix * AffineMatrix3D(
                1.0, 0.0,  0.0, 0.0,
                0.0, cos, -sin, 0.0,
                0.0, sin,  cos, 0.0
            )
        )
    }

    /**
     * Appends a rotation (around the x-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public inline fun rotateX(around: Point, by: Measure<Angle>): AffineTransform = rotateX(around.as3d(), by)

    /**
     * Appends a rotation (around the x-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public fun rotateX(around: Vector3d, by: Measure<Angle>): AffineTransform = this translate around rotateX by translate -around

    /**
     * Appends a rotation (around the y-axis) operation to this transform.
     *
     * @param by this angle
     * @return a new transform
     */
    public infix fun rotateY(by: Measure<Angle>): AffineTransform {
        val sin = sin(by)
        val cos = cos(by)

        return AffineTransform(
            matrix * AffineMatrix3D(
                 cos, 0.0, sin, 0.0,
                 0.0, 1.0, 0.0, 0.0,
                -sin, 0.0, cos, 0.0
            )
        )
    }

    /**
     * Appends a rotation (around the y-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public inline fun rotateY(around: Point, by: Measure<Angle>): AffineTransform = rotateY(around.as3d(), by)

    /**
     * Appends a rotation (around the y-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public fun rotateY(around: Vector3d, by: Measure<Angle>): AffineTransform = this translate around rotateY by translate -around

    /**
     * Applies a vertical flip operation (around the x-axis) to this transform.
     */
    public fun flipVertically(): AffineTransform = scale(1.0, -1.0)

    /**
     * Applies a vertical flip operation to this transform.
     *
     * @param at this y value
     */
    public fun flipVertically(at: Double): AffineTransform = this.translate(y = at).flipVertically().translate(y = -at)

    /**
     * Applies a horizontal flip operation (around the y-axis) to this transform.
     */
    public fun flipHorizontally(): AffineTransform = scale(-1.0,  1.0)

    /**
     * Applies a horizontal flip operation to this transform.
     *
     * @param at this x value
     */
    public fun flipHorizontally(at: Double): AffineTransform = this.translate(x = at).flipHorizontally().translate(x = -at)

    /**
     * The inverse of this transform, if it is invertible. The inverse represents the reverse
     * operation of a transform. So it can be used to "undo".
     *
     * @see AffineMatrix3D.inverse
     */
    public val inverse: AffineTransform? by lazy {
        when {
            isIdentity -> this
            else       -> matrix.inverse?.let {
                when (it.numColumns) {
                    4 -> AffineTransform(
                        it[0, 0], it[0, 1], it[0, 2], it[0,3],
                        it[1, 0], it[1, 1], it[1, 2], it[1,3],
                        it[2, 0], it[2, 1], it[2, 2], it[2,3])
                    else -> AffineTransform(
                        it[0, 0], it[0, 1], it[0, 2],
                        it[1, 1], it[1, 0], it[1, 2])
                }
            }
        }
    }

    /**
     * Applies the transform to [point]. This operation treats [point] as a
     * (3x1 or 4x1) [Matrix][io.nacular.doodle.utils.Matrix] (based on whether this is a 2D or 3D transform)
     * and uses [matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication)
     * to find a new 3x1 matrix to produce the result.
     *
     * ```
     *
     * // Behavior when transform is 2D
     *
     *           |a, b, c|
     * |x, y, 1| |d, e, f| = |xa + yd, xb + ye, ...| -> [xa + yd , xb + ye, 1]
     *           |0, 0, 1|
     * ```
     *
     * @return a new, transformed point
     */
    public inline operator fun invoke(point: Point): Vector3d = this(point.as3d())

    /**
     * Applies the transform to [point]. This operation treats [point] as a
     * (3x1 or 4x1) [Matrix][io.nacular.doodle.utils.Matrix] (based on whether this is a 2D or 3D transform)
     * and uses [matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication)
     * to find a new 3x1 matrix to produce the result.
     *
     * ```
     *
     * // Behavior when transform is 2D
     *
     *           |a, b, c|
     * |x, y, z| |d, e, f| = |xa + yd, xb + ye, ...| -> [xa + yd , xb + ye, zc + zf]
     *           |0, 0, 1|
     * ```
     *
     * @return a new, transformed point
     */
    public operator fun invoke(point: Vector3d): Vector3d = this(listOf(point)).first()

    /**
     * Transforms the given set of points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    @JvmName("invoke2d") public operator fun invoke(points: List<Point>): List<Vector3d> = this(points.map { it.as3d() })

    /**
     * Transforms the given set of points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    public operator fun invoke(points: List<Vector3d>): List<Vector3d> = when {
        isIdentity -> points
        else       -> matrix(points)
    }

    /**
     * Transforms the given polygon. Note that the resulting polygon is a 2D projection of the transformed points.
     * That is because this transform may map the 2D points of [polygon] into a set of 3D points.
     *
     * @param polygon that will be transformed
     * @return a polygon transformed by this object
     */
    public operator fun invoke(polygon: ConvexPolygon): ConvexPolygon = when {
        isIdentity -> polygon
        else       -> this(polygon.points).let { ConvexPolygon(it[0].as2d(), it[1].as2d(), it[2].as2d(), *it.subList(3, it.size).map { it.as2d() }.toTypedArray()) }
    }

    override fun toString(): String = "$matrix"

    override fun hashCode(): Int = matrix.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other                ) return true
        if (other !is AffineTransform     ) return false
        if (isIdentity && other.isIdentity) return true

        if (matrix != other.matrix) return false

        return true
    }

    private operator fun get(vararg values: Double) = values.toList()

    public companion object {
        /**
         * The **identity** transform
         *
         * ```
         * |1 0 0|
         * |0 1 0|
         * |0 0 1|
         * ```
         */
        public val Identity: AffineTransform = AffineTransform()

        /**
         * Creates a transform with the given properties.
         *
         * @param scaleX     how much to scale the x direction
         * @param shearX     how much to shear the x direction
         * @param translateX how much to translate in the x direction
         * @param scaleY     how much to scale the y direction
         * @param shearY     how much to shear the y direction
         * @param translateY how much to translate in the y direction
         */
        public operator fun invoke(
            scaleX    : Double = 1.0,
            shearX    : Double = 0.0,
            translateX: Double = 0.0,
            scaleY    : Double = 1.0,
            shearY    : Double = 0.0,
            translateY: Double = 0.0): AffineTransform = AffineTransform(AffineMatrix3D(scaleX, shearX, translateX, shearY, scaleY, translateY))

        /**
         * Creates a transform with the given properties.
         */
        public operator fun invoke(
            m00: Double = 1.0, m01: Double = 0.0, m02: Double = 0.0, m03: Double = 0.0,
            m10: Double = 0.0, m11: Double = 1.0, m12: Double = 0.0, m13: Double = 0.0,
            m20: Double = 0.0, m21: Double = 0.0, m22: Double = 1.0, m23: Double = 0.0): AffineTransform = AffineTransform(AffineMatrix3D(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23))
    }
}

/**
 * Return the angle this transform would apply.
 *
 * NOTE: This is a computed value requires square roots and
 * trigonometric functions.
 *
 * @return the angle
 */
public fun AffineTransform.computeAngle(): Measure<Angle> {
    val sign  = atan(-shearX / scaleX)
    val angle = acos(scaleX / sqrt((scaleX * scaleX) + (shearX * shearX)))

    return when {
        angle > 90 * degrees && sign > 0 -> 360 * degrees - angle
        angle < 90 * degrees && sign < 0 -> 360 * degrees - angle
        else                             -> angle
    }
}

public operator fun AffineTransform.times(value: Number): AffineTransform = AffineTransform(matrix * value)

public operator fun AffineTransform.div(value: Number): AffineTransform = AffineTransform(matrix / value)

public operator fun AffineTransform.plus(other: AffineTransform): AffineTransform = AffineTransform(matrix + other.matrix)

public operator fun AffineTransform.minus(other: AffineTransform): AffineTransform = AffineTransform(matrix - other.matrix)