package io.nacular.doodle.theme

import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.controls.theme.ProgressIndicatorBehavior
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.theme.PathProgressIndicatorBehavior.Direction.Backward
import io.nacular.doodle.theme.PathProgressIndicatorBehavior.Direction.Forward
import kotlin.math.max
import kotlin.math.min

/**
 * Indicates progress by drawing a [Stroke] over the given [Path].
 *
 * @property pathMetrics used to measure the given path
 * @property path over which the progress will be displayed
 * @property foreground paint to draw the progress in
 * @property background paint to draw the path "behind" the progress line
 * @property foregroundThickness of the path line
 * @property backgroundThickness of the path "behind" the progress line
 * @property direction to draw the progress line
 *
 * @constructor
 * @param pathMetrics used to measure the given path
 * @param path over which the progress will be displayed
 * @param foreground paint to draw the progress in
 * @param background paint to draw the path "behind" the progress line
 * @param foregroundThickness of the path line
 * @param backgroundThickness of the path "behind" the progress line
 * @param direction to draw the progress line
 */
public class PathProgressIndicatorBehavior(
        private val pathMetrics        : PathMetrics,
        public  val path               : Path,
        public  var foreground         : Paint?    = null,
        public  var background         : Paint?    = null,
        public  var foregroundThickness: Double    = 1.0,
        public  var backgroundThickness: Double    = foregroundThickness,
        public  var direction          : Direction = Forward
): ProgressIndicatorBehavior<ProgressIndicator>() {
    /**
     * Indicates the direction along a [Path].
     * [Forward] goes with the path, while [Backward] goes opposite to it
     */
    public enum class Direction {
        Forward, Backward
    }

    private val maxThickNess = max(if (background != null) backgroundThickness else 0.0, foregroundThickness)

    private val pathBounds by lazy { pathMetrics.bounds(path) }
    private val pathLength by lazy { pathMetrics.length(path) }

    override fun install(view: ProgressIndicator) {
        super.install(view)

        view.idealSize = pathBounds.size
    }

    override fun render(view: ProgressIndicator, canvas: Canvas) {
        val fFill = foreground ?: view.foregroundColor?.let { ColorPaint(it) }
        val bFill = background ?: view.backgroundColor?.let { ColorPaint(it) }

        if (fFill == null && bFill == null) {
            return
        }

        val aspectRatio  = pathBounds.width / pathBounds.height
        val targetWidth  = min(view.width,  view.height * aspectRatio) - maxThickNess
        val targetHeight = min(view.height, view.width  / aspectRatio) - maxThickNess
        val scale        = when {
            targetWidth < targetHeight -> targetWidth  / pathBounds.width
            else                       -> targetHeight / pathBounds.height
        }
        val transform    = Identity.
            scale(around = Point(view.width / 2, view.height / 2), x = scale, y = scale).
            translate(x = (view.width - pathBounds.width)/2 - pathBounds.x, y = (view.height - pathBounds.height)/2 - pathBounds.y)

        canvas.transform(transform) {
            bFill?.let {
                canvas.path(path, Stroke(fill = it, thickness = backgroundThickness / scale))
            }

            fFill?.let {
                val offset = when (direction) {
                    Forward -> pathLength - pathLength * view.progress
                    else    -> pathLength + pathLength * view.progress
                }

                when (view.progress) {
                    1.0  -> canvas.path(path, Stroke(fill = it, thickness = foregroundThickness / scale))
                    else -> canvas.path(path, Stroke(
                            fill       = it,
                            thickness  = foregroundThickness / scale,
                            dashOffset = offset,
                            dashes     = doubleArrayOf(pathLength)))
                }
            }
        }
    }

    public companion object {
        /**
         * Creates a behavior that indicates progress by drawing a [Stroke] over the given [Path].
         *
         * @param pathMetrics used to measure the given path
         * @param path over which the progress will be displayed
         * @param foreground color to draw the progress in
         * @param background color to draw the path "behind" the progress line
         * @param foregroundThickness of the path line
         * @param backgroundThickness of the path "behind" the progress line
         * @param direction to draw the progress line
         */
        public operator fun invoke(
                pathMetrics        : PathMetrics,
                path               : Path,
                foreground         : Color?    = null,
                background         : Color?    = null,
                foregroundThickness: Double    = 1.0,
                backgroundThickness: Double    = foregroundThickness,
                direction          : Direction = Forward
        ): PathProgressIndicatorBehavior = PathProgressIndicatorBehavior(
                pathMetrics,
                path,
                foreground?.paint,
                background?.paint,
                foregroundThickness,
                backgroundThickness,
                direction
        )
    }
}