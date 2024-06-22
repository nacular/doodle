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
import io.nacular.doodle.theme.PaintMapper
import io.nacular.doodle.utils.RotationDirection
import io.nacular.doodle.utils.RotationDirection.Clockwise
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.math.min

/**
 * Basic behavior for circular [ProgressIndicator] that uses a filling ring to indicate progress.
 *
 * @property foreground paint used to fill the progress portion
 * @property background paint used to fill the "groove"
 * @property thickness  of the ring (including any [outline])
 * @property startAngle where the progress begins
 * @property direction  progress is drawn around the ring
 * @property startCap   drawn at the tip of the starting point on the progress ring
 * @property endCap     drawn at the tip of the ending point on the progress ring
 *
 * @constructor
 * @param foreground paint used to fill the progress portion
 * @param background paint used to fill the "groove"
 * @param thickness  of the ring (including any [outline])
 * @param startAngle where the progress begins
 * @param direction  progress is drawn around the ring
 * @param startCap   drawn at the tip of the starting point on the progress ring
 * @param endCap     drawn at the tip of the ending point on the progress ring
 */
public class BasicCircularProgressIndicatorBehavior(
    public var foreground: ((ProgressIndicator) -> Paint ),
    public var background: ((ProgressIndicator) -> Paint )? = null,
    public var thickness : Double,
    public var outline   : ((ProgressIndicator) -> Stroke)? = null,
    public var startAngle: Measure<Angle>    = -90 * degrees,
    public var direction : RotationDirection = Clockwise,
    public var startCap  : SegmentBuilder    = { _,_  ->            },
    public var endCap    : SegmentBuilder    = { _,it -> lineTo(it) }
): ProgressIndicatorBehavior<ProgressIndicator>() {
    /**
     * @constructor
     * @param foreground paint used to fill the progress portion
     * @param background paint used to fill the "groove"
     * @param thickness  of the ring (including any [outline])
     * @param startAngle where the progress begins
     * @param direction  progress is drawn around the ring
     * @param startCap   drawn at the tip of the starting point on the progress ring
     * @param endCap     drawn at the tip of the ending point on the progress ring
     */
    public constructor(
        foreground: Paint,
        background: Paint?            = null,
        thickness : Double,
        outline   : Stroke?           = null,
        startAngle: Measure<Angle>    = -90 * degrees,
        direction : RotationDirection = Clockwise,
        startCap  : SegmentBuilder    = { _,_  ->            },
        endCap    : SegmentBuilder    = { _,it -> lineTo(it) }
    ): this(
        foreground = { foreground },
        background = background?.let { { background } },
        thickness  = thickness,
        outline    = outline?.let { { outline } },
        startAngle = startAngle,
        direction  = direction,
        startCap   = startCap,
        endCap     = endCap,
    )

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: ProgressIndicator, canvas: Canvas) {
        val o = outline?.let { it(view) }

        val stroke = o?.let { if (thickness > 2 * it.thickness) o else null }

        val center          = (view.size / 2.0).run { Point(width, height) }
        val outerRadius     = min(view.width, view.height) / 2
        val innerRadius     = maxOf(0.0, outerRadius - thickness)
        val outlineOffset   = (stroke?.let { stroke.thickness / 2 } ?: 0.0)
        val ringOuterRadius = outerRadius - outlineOffset
        val ringInnerRadius = innerRadius + outlineOffset
        val bground         = background?.let { if (view.enabled) it(view) else disabledPaintMapper(it(view)) }
        val fground         = if (view.enabled) foreground(view) else disabledPaintMapper(foreground(view))

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
            view.progress < 1.0 -> canvas.path(ringSection(center, ringInnerRadius, ringOuterRadius, start = startAngle, end = operation(startAngle, 360 * degrees * view.progress), startCap, endCap), fground)
            else                -> canvas.path(ring       (center, ringInnerRadius, ringOuterRadius),                                                                                                   fground)
        }
    }

    public companion object {
        public val circularEndCap        : SegmentBuilder = { _, it -> arcTo(it, 1.0, 1.0, largeArch = true, sweep = true  ) }
        public val negativeCircularEndCap: SegmentBuilder = { _, it -> arcTo(it, 1.0, 1.0, largeArch = true, sweep = false ) }
    }
}
