package io.nacular.doodle.drawing

import io.nacular.doodle.core.Internal
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.AffineTransform.Companion.invoke
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Vector3d
import io.nacular.doodle.geometry.times
import io.nacular.doodle.utils.AffineMatrix3D
import io.nacular.doodle.utils.SquareMatrix
import io.nacular.doodle.utils.matrixOf
import io.nacular.doodle.utils.times
import io.nacular.measured.units.times
import kotlin.jvm.JvmName

/**
 * Represents a 3D transform similar to an [AffineTransform], but it does not guarantee
 * the preservation of parallel lines. These transforms are used whenever a perspective
 * projection is needed to properly render an item.
 */
public class ProjectionTransform internal constructor(@Internal public val matrix: SquareMatrix<Double>) {
    /** `true` if this transform is equal to the [Identity] transform */
    public val isIdentity: Boolean = matrix.isIdentity

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
    public operator fun times(other: AffineTransform): ProjectionTransform = when {
        other.is3d -> ProjectionTransform(matrix * other.matrix       )
        else       -> ProjectionTransform(matrix * other.as3d().matrix)
    }

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
    public operator fun times(other: ProjectionTransform): ProjectionTransform = ProjectionTransform(matrix * other.matrix)

    public inline operator fun invoke(point: Point): Vector3d = this(point.as3d())

    public operator fun invoke(point: Vector3d): Vector3d = this(listOf(point)).first()

    /**
     * Transforms the given set of points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    @JvmName("call") public operator fun invoke(points: List<Point>): List<Vector3d> = this(points.map { it.as3d() })

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
     * That is because this transform may map the 2D points of [polygon] into a set of 3d points.
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
        if (other !is ProjectionTransform ) return false
        if (isIdentity && other.isIdentity) return true

        if (matrix != other.matrix) return false

        return true
    }

    public companion object {
        /**
         * The **identity** transform
         *
         * ```
         * |1 0 0 0|
         * |0 1 0 0|
         * |0 0 1 0|
         * |0 0 0 1|
         * ```
         */
        public val Identity: ProjectionTransform = ProjectionTransform(
            AffineMatrix3D(arrayOf(
                arrayOf(1.0, 0.0, 0.0, 0.0),
                arrayOf(0.0, 1.0, 0.0, 0.0),
                arrayOf(0.0, 0.0, 1.0, 0.0),
                arrayOf(0.0, 0.0, 0.0, 1.0),
            ))
        )
    }
}

public operator fun AffineTransform.times(transform: ProjectionTransform): ProjectionTransform = when {
    isIdentity -> transform
    is3d       -> ProjectionTransform(matrix        * transform.matrix)
    else       -> ProjectionTransform(as3d().matrix * transform.matrix)
}

internal val SquareMatrix<Double>.is3d: Boolean get() = !isIdentity && numColumns > 3

internal operator fun SquareMatrix<Double>.invoke(point: Vector3d): Vector3d = this(listOf(point)).first()

internal operator fun SquareMatrix<Double>.invoke(points: List<Vector3d>): List<Vector3d> = when {
    isIdentity -> points
    else       -> points.map {
        val list    = arrayOf(arrayOf(it.x), arrayOf(it.y), if (is3d) arrayOf(it.z) else arrayOf(1.0), arrayOf(1.0))
        val l: (Int, Int) -> Double = { row, col -> list[row][col] }
        val point   = matrixOf(numRows, 1, l)
        val product = this * point

        when (val w = product[numRows - 1, 0]) {
            1.0, 0.0 -> Vector3d(product[0, 0],     product[1, 0],     product[2, 0]    )
            else     -> Vector3d(product[0, 0] / w, product[1, 0] / w, product[2, 0] / w)
        }
    }
}