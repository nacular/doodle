package io.nacular.doodle.drawing

import io.nacular.doodle.core.Internal
import io.nacular.doodle.drawing.AffineTransform.Companion.invoke
import io.nacular.doodle.drawing.AffineTransform2D.Companion.invoke
import io.nacular.doodle.drawing.AffineTransformImpl.Companion.invoke
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Vector3D
import io.nacular.doodle.geometry.times
import io.nacular.doodle.utils.AffineMatrix3D
import io.nacular.doodle.utils.times
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.acos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.jvm.JvmName
import kotlin.math.atan
import kotlin.math.sqrt

/**
 * Represents an [Affine Transformation](https://en.wikipedia.org/wiki/Affine_transformation) that
 * supports 3D.
 */
public abstract class AffineTransform internal constructor(internal val matrix: AffineMatrix3D) {
    /** `true` if this transform is equal to the [Identity] transform */
    public val isIdentity: Boolean = matrix.isIdentity

    public abstract val inverse: AffineTransform?

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
    public val scaleZ: Double get() = m22

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
    public abstract operator fun times(other: AffineTransform): AffineTransform

    /**
     * Append a scale operation (around the [Origin][io.nacular.doodle.geometry.Point.Origin]) to this transform.
     *
     * @param x amount to scale in the x-direction
     * @param y amount to scale in the y-direction
     * @param z amount to scale in the z-direction
     * @return a new transform
     */
    public abstract fun scale(x: Double = 1.0, y: Double = 1.0, z: Double = 1.0): AffineTransform

    /**
     * Append a scale operation to this transform, that scales around the given point.
     *
     * @param around this point
     * @param x amount to scale in the x-direction
     * @param y amount to scale in the y-direction
     * @param z amount to scale in the z-direction
     * @return a new transform
     */
    public fun scale(around: Point, x: Double = 1.0, y: Double = 1.0, z: Double = 1.0): AffineTransform = scale(around.as3d(), x, y, z)

    public fun scale(around: Vector3D, x: Double = 1.0, y: Double = 1.0, z: Double = 1.0): AffineTransform = (this translate around).scale(x, y, z) translate -around

    /**
     * Append a translation operation to this transform.
     *
     * @param by the x and y components of this point
     * @see translate
     * @return a new transform
     */
    public open infix fun translate(by: Point): AffineTransform = translate(by.x, by.y, 0.0)

    /**
     * Append a translation operation to this transform.
     *
     * @param by the x, y and z components of this vector
     * @see translate
     * @return a new transform
     */
    public infix fun translate(by: Vector3D): AffineTransform = translate(by.x, by.y, by.z)

    /**
     * Append a translation operation to this transform.
     *
     * @param x component
     * @param y component
     * @param z component
     * @see translate
     * @return a new transform
     */
    public abstract fun translate(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): AffineTransform

    /**
     * Append a skew operation to this transform.
     *
     * @param x component
     * @param y component
     * @param z component
     * @return a new transform
     */
    public abstract fun skew(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): AffineTransform

    /**
     * Appends a rotation operation (around the z-axis), at the [Origin][io.nacular.doodle.geometry.Point.Origin] to this transform.
     *
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public open infix fun rotate(by: Measure<Angle>): AffineTransform = rotateZ(by)

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public open fun rotate(around: Point, by: Measure<Angle>): AffineTransform = rotateZ(around, by)

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public fun rotate(around: Vector3D, by: Measure<Angle>): AffineTransform = rotateZ(around, by)

    /**
     * Appends a rotation operation (around the z-axis), at the [Origin][io.nacular.doodle.geometry.Point.Origin] to this transform.
     *
     * @param by this angle
     * @return a new transform
     */
    public abstract infix fun rotateZ(by: Measure<Angle>): AffineTransform

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public fun rotateZ(around: Point, by: Measure<Angle>): AffineTransform = rotateZ(around.as3d(), by)

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public fun rotateZ(around: Vector3D, by: Measure<Angle>): AffineTransform = this translate around rotateZ by translate -around

    /**
     * Appends a rotation operation (around the x-axis), at the [Origin][io.nacular.doodle.geometry.Point.Origin] to this transform.
     *
     * @param by this angle
     * @return a new transform
     */
    public abstract infix fun rotateX(by: Measure<Angle>): AffineTransform

    /**
     * Appends a rotation (around the x-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @return a new transform
     */
    public fun rotateX(around: Point, by: Measure<Angle>): AffineTransform = rotateX(around.as3d(), by)

    /**
     * Appends a rotation (around the x-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @return a new transform
     */
    public fun rotateX(around: Vector3D, by: Measure<Angle>): AffineTransform = this translate around rotateX by translate -around

    /**
     * Appends a rotation (around the y-axis) operation to this transform.
     *
     * @param by this angle
     * @return a new transform
     */
    public abstract infix fun rotateY(by: Measure<Angle>): AffineTransform

    /**
     * Appends a rotation (around the y-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @return a new transform
     */
    public fun rotateY(around: Point, by: Measure<Angle>): AffineTransform = rotateY(around.as3d(), by)

    /**
     * Appends a rotation (around the y-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @return a new transform
     */
    public fun rotateY(around: Vector3D, by: Measure<Angle>): AffineTransform = this translate around rotateY by translate -around

    /**
     * Applies a vertical flip operation (around the x-axis) to this transform.
     */
    public open fun flipVertically(): AffineTransform = scale(1.0, -1.0)

    /**
     * Applies a vertical flip operation to this transform.
     *
     * @param at this y value
     */
    public open fun flipVertically(at: Double): AffineTransform = this.translate(y = at).flipVertically().translate(y = -at)

    /**
     * Applies a horizontal flip operation (around the y-axis) to this transform.
     */
    public open fun flipHorizontally(): AffineTransform = scale(-1.0,  1.0)

    /**
     * Applies a horizontal flip operation to this transform.
     *
     * @param at this x value
     */
    public open fun flipHorizontally(at: Double): AffineTransform = this.translate(x = at).flipHorizontally().translate(x = -at)

    /**
     * Applies the transform to [point]. This operation treats [point] as a
     * (3x1 or 4x1) [Matrix][io.nacular.doodle.utils.Matrix] (based on whether this is a 2D or 3D transform)
     * and uses [matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication)
     * to find a new 3x1 matrix to produce the result.
     *
     * ```
     *
     *              |a, b, c, d|
     * |x, y, 0, 1| |e, f, g, h| = |xa + ye, xb + yf, 1| -> [xa + ye, xb + ye]
     *              |i, j, k, l|
     *              |0, 0, 0, 1|
     * ```
     *
     * @return a new, transformed point
     */
    public abstract operator fun invoke(point: Point): Vector3D

    /**
     * Applies the transform to [point]. This operation treats [point] as a
     * (3x1 or 4x1) [Matrix][io.nacular.doodle.utils.Matrix] (based on whether this is a 2D or 3D transform)
     * and uses [matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication)
     * to find a new 3x1 matrix to produce the result.
     *
     * ```
     *
     *              |a, b, c, d|
     * |x, y, z, 1| |e, f, g, h| = |xa + ye + zi, xb + yf + zj, xc + yg + zk, 1|
     *              |i, j, k, l|
     *              |0, 0, 0, 1|
     *
     *                          -> [xa + ye + zi, xb + yf + zj, xc + yg + zk]
     * ```
     *
     * @return a new, transformed point
     */
    public abstract fun invoke(point: Vector3D): Vector3D

    /**
     * Transforms the given points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    public abstract operator fun invoke(vararg points: Vector3D): List<Vector3D>

    /**
     * Transforms the given polygon. Note that the resulting polygon is a 2D projection of the transformed points.
     * That is because this transform may map the 2D points of [polygon] into a set of 3D points.
     *
     * @param polygon that will be transformed
     * @return a polygon transformed by this object
     */
    public operator fun invoke(polygon: ConvexPolygon): ConvexPolygon = when {
        isIdentity -> polygon
        else       -> this(*polygon.points.toTypedArray()).let { ConvexPolygon(it[0].as2d(), it[1].as2d(), it[2].as2d(), *it.subList(3, it.size).map { it.as2d() }.toTypedArray()) }
    }

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
        public val Identity: AffineTransform2D = AffineTransformImpl()

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
            translateY: Double = 0.0): AffineTransform2D = AffineTransformImpl(AffineMatrix3D(scaleX, shearX, translateX, shearY, scaleY, translateY))

        /**
         * Creates a transform with the given properties.
         */
        public operator fun invoke(
            m00: Double = 1.0, m01: Double = 0.0, m02: Double = 0.0, m03: Double = 0.0,
            m10: Double = 0.0, m11: Double = 1.0, m12: Double = 0.0, m13: Double = 0.0,
            m20: Double = 0.0, m21: Double = 0.0, m22: Double = 1.0, m23: Double = 0.0): AffineTransform = AffineTransformImpl(AffineMatrix3D(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23))
    }
}

/**
 * Represents an [Affine Transformation](https://en.wikipedia.org/wiki/Affine_transformation) that
 * supports only 2D.
 */
public abstract class AffineTransform2D internal constructor(matrix: AffineMatrix3D): AffineTransform(matrix) {
    public abstract override val inverse: AffineTransform2D?

    /**
     * Allows transforms to be combined sequentially.
     *
     * ```
     *
     * val transformC = transformA * transformB
     * ```
     *  So applying `transformC` is equivalent to applying `transformA` then `transformB`.
     *
     * @see times
     */
    public abstract operator fun times(other: AffineTransform2D): AffineTransform2D

    /**
     * Append a scale operation (around the [Origin][io.nacular.doodle.geometry.Point.Origin]) to this transform.
     *
     * @param x amount to scale in the x-direction
     * @param y amount to scale in the y-direction
     * @return a new transform
     */
    public abstract fun scale(x: Double = 1.0, y: Double = 1.0): AffineTransform2D

    /**
     * Append a scale operation to this transform, that scales around the given point.
     *
     * @param around this point
     * @param x amount to scale in the x-direction
     * @param y amount to scale in the y-direction
     * @return a new transform
     */
    public fun scale(around: Point, x: Double = 1.0, y: Double = 1.0): AffineTransform2D = (this translate around).scale(x, y) translate -around

    /**
     * Append a translation operation to this transform.
     *
     * @param by the x and y components of this point
     * @see translate
     * @return a new transform
     */
    public override infix fun translate(by: Point): AffineTransform2D = translate(by.x, by.y)

    /**
     * Append a translation operation to this transform.
     *
     * @param x component
     * @param y component
     * @see translate
     * @return a new transform
     */
    public abstract fun translate(x: Double = 0.0, y: Double = 0.0): AffineTransform2D

    /**
     * Append a skew operation to this transform.
     *
     * @param x component
     * @param y component
     * @return a new transform
     */
    public abstract fun skew(x: Double = 0.0, y: Double = 0.0): AffineTransform2D

    /**
     * Appends a rotation operation (around the z-axis), at the [Origin][io.nacular.doodle.geometry.Point.Origin] to this transform.
     *
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public abstract override infix fun rotate(by: Measure<Angle>): AffineTransform2D

    /**
     * Appends a rotation (around the z-axis) operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotateZ
     * @return a new transform
     */
    public override fun rotate(around: Point, by: Measure<Angle>): AffineTransform2D = this translate around rotate by translate -around

    /**
     * Applies a vertical flip operation (around the x-axis) to this transform.
     */
    public override fun flipVertically(): AffineTransform2D = scale(1.0, -1.0)

    /**
     * Applies a vertical flip operation to this transform.
     *
     * @param at this y value
     */
    public override fun flipVertically(at: Double): AffineTransform2D = this.translate(y = at).flipVertically().translate(y = -at)

    /**
     * Applies a horizontal flip operation (around the y-axis) to this transform.
     */
    public override fun flipHorizontally(): AffineTransform2D = scale(-1.0,  1.0)

    /**
     * Applies a horizontal flip operation to this transform.
     *
     * @param at this x value
     */
    public override fun flipHorizontally(at: Double): AffineTransform2D = this.translate(x = at).flipHorizontally().translate(x = -at)

    /**
     * Transforms the given points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    public abstract operator fun invoke(vararg points: Point): List<Point>

    public companion object {
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
            translateY: Double = 0.0): AffineTransform2D = AffineTransformImpl(AffineMatrix3D(scaleX, shearX, translateX, shearY, scaleY, translateY))
    }
}

internal class AffineTransformImpl(matrix: AffineMatrix3D): AffineTransform2D(matrix) {
    override val inverse: AffineTransformImpl? by lazy {
        when {
            isIdentity -> this
            else       -> matrix.inverse?.let {
                when (it.numColumns) {
                    4 -> AffineTransformImpl(
                        it[0, 0], it[0, 1], it[0, 2], it[0,3],
                        it[1, 0], it[1, 1], it[1, 2], it[1,3],
                        it[2, 0], it[2, 1], it[2, 2], it[2,3])
                    else -> AffineTransformImpl(
                        it[0, 0], it[0, 1], it[0, 2],
                        it[1, 1], it[1, 0], it[1, 2])
                }
            }
        }
    }

    override fun times(other: AffineTransform2D): AffineTransform2D = AffineTransformImpl(matrix * other.matrix)

    override fun times(other: AffineTransform): AffineTransform = AffineTransformImpl(matrix * other.matrix)

    override fun scale(x: Double, y: Double): AffineTransform2D = scale(x, y, z = 1.0)

    override fun scale(x: Double, y: Double, z: Double): AffineTransformImpl = AffineTransformImpl(
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

    override fun translate(x: Double, y: Double): AffineTransform2D = translate(x, y, z = 0.0)

    override fun translate(x: Double, y: Double, z: Double): AffineTransformImpl = AffineTransformImpl(
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

    override fun skew(x: Double, y: Double): AffineTransform2D = skew(x, y, z = 0.0)

    override fun skew(x: Double, y: Double, z: Double): AffineTransformImpl = AffineTransformImpl(
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

    override inline fun rotate(by: Measure<Angle>): AffineTransformImpl = rotateZ(by)

    override fun rotateZ(by: Measure<Angle>): AffineTransformImpl {
        val sin = Angle.sin(by)
        val cos = Angle.cos(by)

        return AffineTransformImpl(
            matrix * AffineMatrix3D(
                cos, -sin, 0.0,
                sin,  cos, 0.0)
        )
    }

    override fun rotateX(by: Measure<Angle>): AffineTransform {
        val sin = Angle.sin(by)
        val cos = Angle.cos(by)

        return AffineTransformImpl(
            matrix * AffineMatrix3D(
                1.0, 0.0,  0.0, 0.0,
                0.0, cos, -sin, 0.0,
                0.0, sin,  cos, 0.0
            )
        )
    }

    override fun rotateY(by: Measure<Angle>): AffineTransform {
        val sin = Angle.sin(by)
        val cos = Angle.cos(by)

        return AffineTransformImpl(
            matrix * AffineMatrix3D(
                cos, 0.0, sin, 0.0,
                0.0, 1.0, 0.0, 0.0,
                -sin, 0.0, cos, 0.0
            )
        )
    }

    override fun invoke(point: Point): Point = when {
        isIdentity -> point
        else       -> matrix(point.as3d()).as2d()
    }

    override fun invoke(point: Vector3D): Vector3D = when {
        isIdentity -> point
        else       -> matrix(point)
    }

    override fun invoke(vararg points: Point): List<Point> = this(points = (points.map { it.as3d() }.toTypedArray())).map { it.as2d() }

    override fun invoke(vararg points: Vector3D): List<Vector3D> = when {
        isIdentity -> points.toList()
        else       -> matrix(points = points)
    }

    override fun toString(): String = "$matrix"

    override fun hashCode(): Int = matrix.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other                 ) return true
        if (other !is AffineTransformImpl) return false
        if (isIdentity && other.isIdentity ) return true

        if (matrix != other.matrix) return false

        return true
    }

    companion object {
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
        operator fun invoke(
            scaleX    : Double = 1.0,
            shearX    : Double = 0.0,
            translateX: Double = 0.0,
            scaleY    : Double = 1.0,
            shearY    : Double = 0.0,
            translateY: Double = 0.0): AffineTransformImpl = AffineTransformImpl(AffineMatrix3D(scaleX, shearX, translateX, shearY, scaleY, translateY))

        /**
         * Creates a transform with the given properties.
         */
        operator fun invoke(
            m00: Double = 1.0, m01: Double = 0.0, m02: Double = 0.0, m03: Double = 0.0,
            m10: Double = 0.0, m11: Double = 1.0, m12: Double = 0.0, m13: Double = 0.0,
            m20: Double = 0.0, m21: Double = 0.0, m22: Double = 1.0, m23: Double = 0.0): AffineTransformImpl = AffineTransformImpl(AffineMatrix3D(
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
public fun AffineTransform2D.computeAngle(): Measure<Angle> {
    val sign  = atan(-shearX / scaleX)
    val angle = acos(scaleX / sqrt((scaleX * scaleX) + (shearX * shearX)))

    return when {
        angle > 90 * degrees && sign > 0 -> 360 * degrees - angle
        angle < 90 * degrees && sign < 0 -> 360 * degrees - angle
        else                             -> angle
    }
}

/**
 * Transforms the given points.
 *
 * @param points that will be transformed
 * @return a list of points transformed by this object
 * @see invoke
 */
@JvmName("callPoint")
public inline fun AffineTransform.invoke(points: List<Point>): List<Vector3D> = this(points = points.toTypedArray())

/**
 * Transforms the given points.
 *
 * @param points that will be transformed
 * @return a list of points transformed by this object
 * @see invoke
 */
public inline fun AffineTransform.invoke(points: List<Vector3D>): List<Vector3D> = this(points = points.toTypedArray())

/**
 * Transforms the given points.
 *
 * @param points that will be transformed
 * @return a list of points transformed by this object
 * @see invoke
 */
@JvmName("callPoint")
public inline fun AffineTransform2D.invoke(points: List<Point>): List<Point> = this(points = points.toTypedArray())

/**
 * Transforms the given points.
 *
 * @param points that will be transformed
 * @return a list of points transformed by this object
 * @see invoke
 */
public inline fun AffineTransform2D.invoke(points: List<Vector3D>): List<Vector3D> = this(points = points.toTypedArray())
