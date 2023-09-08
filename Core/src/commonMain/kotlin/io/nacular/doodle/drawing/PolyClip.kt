package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Plane
import io.nacular.doodle.geometry.Ray
import io.nacular.doodle.geometry.Vector3D

/**
 * Performs the [Sutherland-Hodgman](https://en.wikipedia.org/wiki/Sutherland%E2%80%93Hodgman_algorithm) clipping
 * algorithm.
 *
 * @param convexPoly to clip
 * @param to a specified plane
 * @return list of clipped points, which may be an empty
 */
internal fun clip(convexPoly: List<Vector3D>, to: Plane): List<Vector3D> {
    val result = mutableListOf<Vector3D>()

    for (i in convexPoly.indices) {
        val p = convexPoly[i]
        val q = convexPoly[(i + 1) % convexPoly.size]

        val pInFront = p.inFrontOf(to)
        val qInFront = q.inFrontOf(to)

        when {
            qInFront && pInFront  -> result += q
            pInFront && !qInFront -> result += to.intersection(with = Ray(p, q - p))!!
            qInFront && !pInFront -> {
                result += to.intersection(with = Ray(p, q - p))!!
                result += q
            }
        }
    }

    return result
}

/**
 * Check whether this vector is in front of [plane].
 */
private fun Vector3D.inFrontOf(plane: Plane): Boolean = plane.direction * (plane.position - this) <= 0.0