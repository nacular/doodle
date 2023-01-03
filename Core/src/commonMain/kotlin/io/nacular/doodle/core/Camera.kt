package io.nacular.doodle.core

import io.nacular.doodle.drawing.ProjectionTransform
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.utils.SquareMatrix
import io.nacular.doodle.utils.times

/**
 * Defines the perspective a viewer has if they are assumed to be looking directly at the
 * screen. A camera placed farther from a [View] will create a stronger perspective
 * projection of that View. This will result in the View looking more 3-dimensional when
 * it is transformed "into" the screen in some way. Whether using the z-axis or because
 * of rotations around the x and/or y-axis.
 *
 * @property position in the coordinate space where the camera center is pointed
 * @property distance from the coordinate surface of the camera
 */
public data class Camera(val position: Point, val distance: Double) {

    /**
     * The transform this Camera will apply to objects "seen" through it.
     * This is helpful when trying to compute the location of a point in
     * the "world" from the Camera's perspective.
     */
    public val projection: ProjectionTransform by lazy { projection(offset = Origin) }

    /** @suppress */
    @Internal public fun projection(offset: Point): ProjectionTransform = when (distance) {
        0.0, Double.MAX_VALUE -> ProjectionTransform.Identity
        else                  -> {
            val p = position + offset

            ProjectionTransform(SquareMatrix(arrayOf(
                arrayOf(1.0, 0.0, 0.0, p.x),
                arrayOf(0.0, 1.0, 0.0, p.y),
                arrayOf(0.0, 0.0, 1.0, 0.0),
                arrayOf(0.0, 0.0, 0.0, 1.0))
            ) * SquareMatrix(arrayOf(
                arrayOf(1.0, 0.0, 0.0,           0.0),
                arrayOf(0.0, 1.0, 0.0,           0.0),
                arrayOf(0.0, 0.0, 1.0,           0.0),
                arrayOf(0.0, 0.0, -1 / distance, 1.0))
            ) * SquareMatrix(arrayOf(
                arrayOf(1.0, 0.0, 0.0, -p.x),
                arrayOf(0.0, 1.0, 0.0, -p.y),
                arrayOf(0.0, 0.0, 1.0,  0.0),
                arrayOf(0.0, 0.0, 0.0,  1.0))
            ))
        }
    }

    public companion object {
        public val Identity: Camera = Camera(Origin, Double.MAX_VALUE)
    }
}
