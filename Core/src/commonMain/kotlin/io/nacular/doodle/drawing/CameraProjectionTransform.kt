package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Plane
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Vector3D
import io.nacular.doodle.utils.SquareMatrix
import io.nacular.doodle.utils.times

/**
 * Created by Nicholas Eddy on 7/19/23.
 */
internal class CameraProjectionTransform(matrix: SquareMatrix<Double>): ProjectionTransform(matrix) {

    override operator fun times(other: AffineTransform): ProjectionTransform = when {
        other.is3d -> CameraProjectionTransform(matrix * other.matrix       )
        else       -> CameraProjectionTransform(matrix * other.as3d().matrix)
    }

    override operator fun times(other: ProjectionTransform): ProjectionTransform = CameraProjectionTransform(matrix * other.matrix)

    /**
     * Transforms the given polygon and clips to avoid points falling behind the camera.
     *
     * @param polygon that will be transformed
     * @return a polygon transformed by this object
     */
    override operator fun invoke(polygon: ConvexPolygon): ConvexPolygon = when {
        isIdentity -> polygon
        else       -> polygon.points.map { matrix.homogeneous(it) }.let { initial ->
            val planeZ = initial.firstOrNull { it.second < 0 }?.let {
                (it.first.z - 1) / (1 - it.second)
            } ?: 0.0

            val homogeneous = when (planeZ) {
                0.0  -> initial
                else -> clip(
                    convexPoly = initial.map { it.first },
                    to         = Plane(Vector3D(z = planeZ), direction = Vector3D(z = -1))
                ).let {
                    it.map { it to (planeZ - it.z + 1) / planeZ }
                }
            }

            return homogeneous.map { (v, w) ->
                when (w) {
                    0.0, 1.0 -> v.as2d()
                    else     -> Point(v.x / w, v.y / w)
                }
            }.let {
                ConvexPolygon(
                    it[0],
                    it[1],
                    it[2],
                    *it.subList(3, it.size).toTypedArray()
                )
            }
        }
    }

}