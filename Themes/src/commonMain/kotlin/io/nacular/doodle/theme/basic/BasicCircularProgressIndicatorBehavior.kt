package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.controls.theme.ProgressIndicatorBehavior
import io.nacular.doodle.drawing.Canvas
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
 * Uses a filling ring to indicate progress.
 *
 * @property foreground paint used to fill the progress portion
 * @property background paint used to fill the "groove"
 * @property thickness of the ring (including any [outline])
 * @property startAngle where the progress begins
 * @property direction progress is drawn around the ring
 * @property startCap drawn at the tip of the starting point on the progress ring
 * @property endCap drawn at the tip of the ending point on the progress ring
 *
 * @constructor
 * @param foreground paint used to fill the progress portion
 * @param background paint used to fill the "groove"
 * @param thickness of the ring (including any [outline])
 * @param startAngle where the progress begins
 * @param direction progress is drawn around the ring
 * @param startCap drawn at the tip of the starting point on the progress ring
 * @param endCap drawn at the tip of the ending point on the progress ring
 */
public class BasicCircularProgressIndicatorBehavior(
        public var foreground: Paint,
        public var background: Paint?            = null,
        public var thickness : Double,
        public var outline   : Stroke?           = null,
        public var startAngle: Measure<Angle>    = -90 * degrees,
        public var direction : RotationDirection = Clockwise,
        public var startCap  : SegmentBuilder    = { _,it -> lineTo(it) },
        public var endCap    : SegmentBuilder    = { _,_  ->            }
): ProgressIndicatorBehavior<ProgressIndicator>() {
    override fun render(view: ProgressIndicator, canvas: Canvas) {
        val stroke = outline?.let { if (thickness > 2 * it.thickness) outline else null }

        val center          = (view.size / 2.0).run { Point(width, height) }
        val outerRadius     = min(view.width, view.height) / 2
        val innerRadius     = maxOf(0.0, outerRadius - thickness)
        val outlineOffset   = (stroke?.let { stroke.thickness / 2 } ?: 0.0)
        val ringOuterRadius = outerRadius - outlineOffset
        val ringInnerRadius = innerRadius + outlineOffset
        val bground         = background

        when {
            stroke  != null && bground != null -> canvas.path(ring(center, ringInnerRadius, ringOuterRadius), stroke, bground)
            stroke  != null                    -> canvas.path(ring(center, ringInnerRadius, ringOuterRadius), stroke         )
            bground != null                    -> canvas.path(ring(center, ringInnerRadius, ringOuterRadius),         bground)
        }

        val operation: (Measure<Angle>, Measure<Angle>) -> Measure<Angle> = when (direction) {
            Clockwise -> { a, b -> a + b }
            else      -> { a, b -> a - b }
        }

        when {
            view.progress < 1.0 -> canvas.path(ringSection(center, ringInnerRadius, ringOuterRadius, start = startAngle, end = operation(startAngle, 360 * degrees * view.progress), startCap, endCap), foreground)
            else                -> canvas.path(ring       (center, ringInnerRadius, ringOuterRadius),                                                                                                   foreground)
        }
    }

    public companion object {
        public val circularEndCap        : SegmentBuilder = { _, it -> arcTo(it, 1.0, 1.0, largeArch = true, sweep = true  ) }
        public val negativeCircularEndCap: SegmentBuilder = { _, it -> arcTo(it, 1.0, 1.0, largeArch = true, sweep = false ) }
    }
}
