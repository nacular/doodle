package io.nacular.doodle.drawing

import io.nacular.doodle.core.Internal
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Vector3D
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
public open class ProjectionTransform internal constructor(@Internal public val matrix: SquareMatrix<Double>) {
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
    public open operator fun times(other: AffineTransform): ProjectionTransform = when {
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
    public open operator fun times(other: ProjectionTransform): ProjectionTransform = ProjectionTransform(matrix * other.matrix)

    public inline operator fun invoke(point: Point): Vector3D = this(point.as3d())

    public operator fun invoke(point: Vector3D): Vector3D = this(points = arrayOf(point)).first()

    /**
     * Transforms the given points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    public operator fun invoke(vararg points: Point): List<Vector3D> = this(points = points.map { it.as3d() }.toTypedArray())

    /**
     * Transforms the given points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    @JvmName("callPoint")
    public fun invoke(points: List<Point>): List<Vector3D> = this(points = points.toTypedArray())

    /**
     * Transforms the given points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    public fun invoke(points: List<Vector3D>): List<Vector3D> = this(points = points.toTypedArray())

    /**
     * Transforms the given points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     * @see invoke
     */
    public operator fun invoke(vararg points: Vector3D): List<Vector3D> = when {
        isIdentity -> points.toList()
        else       -> matrix(points = points)
    }

    /**
     * Transforms the given polygon. Note that the resulting polygon is a 2D projection of the transformed points.
     * That is because this transform may map the 2D points of [polygon] into a set of 3d points.
     *
     * @param polygon that will be transformed
     * @return a polygon transformed by this object
     */
    public open operator fun invoke(polygon: ConvexPolygon): ConvexPolygon = when {
        isIdentity -> polygon
        else       -> polygon.points.map { matrix.homogeneous(it) }.let { initial ->
            initial.map { (v, w) -> fromHomogeneous(v, w).as2d() }.let {
                ConvexPolygon(
                    it[0],
                    it[1],
                    it[2],
                    *it.subList(3, it.size).toTypedArray()
                )
            }
        }
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

public operator fun AffineTransform2D.times(transform: ProjectionTransform): ProjectionTransform = when {
    isIdentity -> transform
    else       -> ProjectionTransform(as3d().matrix * transform.matrix)
}

public operator fun AffineTransform.times(transform: ProjectionTransform): ProjectionTransform = when {
    isIdentity -> transform
    else       -> ProjectionTransform(matrix * transform.matrix)
}

internal val SquareMatrix<Double>.is3d: Boolean get() = !isIdentity && numColumns > 3

internal operator fun SquareMatrix<Double>.invoke(point: Vector3D): Vector3D = homogeneous(point).let { (v, w) ->
    fromHomogeneous(v, w)
}

private fun fromHomogeneous(homogeneous: Vector3D, w: Double) = when (w) {
    0.0, 1.0 -> homogeneous
    else     -> Vector3D(homogeneous.x / w, homogeneous.y / w, homogeneous.z / w)
}

internal fun SquareMatrix<Double>.homogeneous(point: Vector3D): Pair<Vector3D, Double> = when {
    isIdentity -> point to 1.0
    else       -> {
        val list    = arrayOf(arrayOf(point.x), arrayOf(point.y), if (is3d) arrayOf(point.z) else arrayOf(1.0), arrayOf(1.0))
        val l: (Int, Int) -> Double = { row, col -> list[row][col] }
        val point_   = matrixOf(numRows, 1, l)
        val product = this * point_

        Vector3D(product[0, 0],product[1, 0], product[2, 0]) to product[numRows - 1, 0]
    }
}

internal operator fun SquareMatrix<Double>.invoke(vararg points: Vector3D): List<Vector3D> = points.map { invoke(it) }

internal operator fun SquareMatrix<Double>.invoke(points: List<Vector3D>): List<Vector3D> = when {
    isIdentity -> points
    else       -> points.map { invoke(it) }
}