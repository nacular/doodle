package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.controls.theme.ProgressIndicatorBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.SegmentBuilder
import io.nacular.doodle.geometry.div
import io.nacular.doodle.geometry.ring
import io.nacular.doodle.geometry.ringSection
import io.nacular.doodle.utils.RotationDirection
import io.nacular.doodle.utils.RotationDirection.Clockwise
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.math.min

/**
 *
 */
public class BasicCircularProgressBehavior(
        private val foreground      : Paint,
        private val background      : Paint?             = null,
        private val thickness       : Double,
        private val outlineColor    : Color?            = null,
        private val outlineThickness: Double            = 1.0,
        private val startAngle      : Measure<Angle>    = -90 * degrees,
        private val direction       : RotationDirection = Clockwise,
        private val startCap        : SegmentBuilder    = { _,it -> lineTo(it) },
        private val endCap          : SegmentBuilder    = { _,_  ->            }
): ProgressIndicatorBehavior<ProgressIndicator>() {
    override fun render(view: ProgressIndicator, canvas: Canvas) {
        val stroke = if (outlineColor != null && thickness > 2 * outlineThickness) Stroke(outlineColor, outlineThickness) else null

        val center          = (view.size / 2.0).run { Point(width, height) }
        val outerRadius     = min(view.width, view.height) / 2
        val innerRadius     = maxOf(0.0, outerRadius - thickness)
        val outlineOffset   = (stroke?.let { outlineThickness / 2 } ?: 0.0)
        val ringOuterRadius = outerRadius - outlineOffset
        val ringInnerRadius = innerRadius + outlineOffset

        when {
            stroke     != null && background != null -> canvas.path(ring(center, ringInnerRadius, ringOuterRadius), stroke, background)
            stroke     != null                       -> canvas.path(ring(center, ringInnerRadius, ringOuterRadius), stroke            )
            background != null                       -> canvas.path(ring(center, ringInnerRadius, ringOuterRadius),         background)
        }

        val operation: (Measure<Angle>, Measure<Angle>) -> Measure<Angle> = when (direction) {
            Clockwise -> { a, b -> a + b }
            else      -> { a, b -> a - b }
        }

        when {
            view.progress < 1.0 -> canvas.path(ringSection(center, ringInnerRadius, ringOuterRadius, start = startAngle, end = operation(startAngle, 360 * degrees * view.progress), startCap, endCap), foreground)
            else                -> canvas.path(ring       (center, ringInnerRadius, ringOuterRadius),                                                                                               foreground)
        }
    }

    public companion object {
        public val circularEndCap        : SegmentBuilder = { _, it -> arcTo(it, 1.0, 1.0, largeArch = true, sweep = true  ) }
        public val negativeCircularEndCap: SegmentBuilder = { _, it -> arcTo(it, 1.0, 1.0, largeArch = true, sweep = false ) }
    }
}
