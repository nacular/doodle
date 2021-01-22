package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.AffineMatrix3D
import io.nacular.doodle.utils.matrixOf
import io.nacular.doodle.utils.times
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure

/**
 * Represents an [Affine Transformation](https://en.wikipedia.org/wiki/Affine_transformation).
 *
 * @see AffineMatrix3D to see the underlying matrix used for such a transform.
 */
@Suppress("ReplaceSingleLineLet")
public class AffineTransform private constructor(private val matrix: AffineMatrix3D) {

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
    public val scaleX: Double get() = matrix[0, 0]

    /** Shear component in the x direction */
    public val shearX: Double get() = matrix[0, 1]

    /** Translation component in the x direction */
    public val translateX: Double get() = matrix[0, 2]

    /** Shear component in the y direction */
    public val shearY: Double get() = matrix[1, 0]

    /** Scale component in the y direction */
    public val scaleY: Double get() = matrix[1, 1]

    /** Translation component in the y direction */
    public val translateY: Double get() = matrix[1, 2]

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
     * @return a new transform
     */
    public fun scale(x: Double = 1.0, y: Double = 1.0): AffineTransform = AffineTransform(
            matrix * AffineMatrix3D(
                    x,   0.0, 0.0,
                    0.0,   y, 0.0)
    )

    /**
     * Append a scale operation to this transform, that scales around the given point.
     *
     * @param around this point
     * @param x amount to scale in the x-direction
     * @param y amount to scale in the y-direction
     * @return a new transform
     */
    public fun scale(around: Point, x: Double = 1.0, y: Double = 1.0): AffineTransform = (this translate around).scale(x, y) translate -around

    /**
     * Append a translation operation to this transform.
     *
     * @param by the x and y components of this point
     * @see translate
     * @return a new transform
     */
    public infix fun translate(by: Point): AffineTransform = translate(by.x, by.y)

    /**
     * Append a translation operation to this transform.
     *
     * @param x component
     * @param y component
     * @see translate
     * @return a new transform
     */
    public fun translate(x: Double = 0.0, y: Double = 0.0): AffineTransform = AffineTransform(
            matrix * AffineMatrix3D(
                    1.0, 0.0, x,
                    0.0, 1.0, y)
    )

    /**
     * Append a skew operation to this transform.
     *
     * @param x component
     * @param y component
     * @return a new transform
     */
    public fun skew(x: Double, y: Double): AffineTransform = AffineTransform(
            matrix * AffineMatrix3D(
                    1.0,   x, 0.0,
                      y, 1.0, 0.0)
    )

    /**
     * Appends a rotation operation (around the [Origin][io.nacular.doodle.geometry.Point.Origin]) to this transform.
     *
     * @param by this angle
     * @see rotate
     * @return a new transform
     */
    public infix fun rotate(by: Measure<Angle>): AffineTransform {
        val sin = sin(by)
        val cos = cos(by)

        return AffineTransform(
                matrix * AffineMatrix3D(
                        cos, -sin, 0.0,
                        sin,  cos, 0.0)
        )
    }


    /**
     * Appends a rotation operation to this transform.
     *
     * @param around this point
     * @param by this angle
     * @see rotate
     * @return a new transform
     */
    public fun rotate(around: Point, by: Measure<Angle>): AffineTransform = this translate around rotate by translate -around

    /**
     * Applies a vertical flip operation (around the x-axis) to this transform.
     */
    public fun flipVertically(): AffineTransform = scale (1.0, -1.0)

    /**
     * Applies a vertical flip operation to this transform.
     *
     * @param at this y value
     */
    public fun flipVertically(at: Double): AffineTransform = this.translate(y = at).flipVertically  ().translate(y = -at)

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
            else       -> matrix.inverse?.let { AffineTransform(
                    it[0, 0], it[0, 1], it[0, 2],
                    it[1, 1], it[1, 0], it[1, 2]) }
        }
    }

    /**
     * Applies the transform to [point]. This operation treats [point] as a
     * 3x1 [Matrix][io.nacular.doodle.utils.Matrix] and uses [matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication)
     * to find a new 3x1 matrix that is "truncated" to produce the result.
     *
     * ```
     *           |a, b, c|
     * |x, y, 1| |d, e, f| = |xa + yd, xb + ye, ...| -> [xa + yd , xb + ye]
     *           |0, 0, 1|
     * ```
     *
     * @return a new, transformed point
     */
    public operator fun invoke(point: Point): Point = this(listOf(point)).first()

    /**
     * Transforms the given set of points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     */
    public operator fun invoke(points: List<Point>): List<Point> = when{
        isIdentity -> points
        else       -> points.map {
            val list    = listOf(listOf(it.x), listOf(it.y), listOf(1.0))
            val point   = matrixOf(3, 1) { col, row -> list[row][col] }
            val product = matrix * point

            Point(product[0, 0], product[1, 0])
        }
    }

    /**
     * Transforms the given polygon.
     *
     * @param polygon that will be transformed
     * @return a polygon transformed by this object
     */
    public operator fun invoke(polygon: ConvexPolygon): ConvexPolygon = when {
        isIdentity -> polygon
        else       -> this(polygon.points).let { ConvexPolygon(it[0], it[1], it[2], *it.subList(3, it.size).toTypedArray()) }
    }

    override fun toString(): String = matrix.toString()

    override fun hashCode(): Int = matrix.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other           ) return true
        if (other !is AffineTransform) return false

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
    }
}
