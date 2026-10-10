package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.circle
import io.nacular.doodle.geometry.inset
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.theme.basic.range.MarkLocation.GrooveAndRange
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.RotationDirection.Clockwise
import io.nacular.doodle.utils.RotationDirection.CounterClockwise
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times

/**
 * Determines where marks are shown for a slider with them shown.
 */
public enum class MarkLocation {
    /**
     * Only draws marks on the slider's groove
     */
    Groove,

    /**
     * Draws marks on the slider's groove and range
     */
    GrooveAndRange
}

/**
 * Configures how marks are drawn for sliders
 *
 * @property gap indicates the size of the mark on the bar
 * @property location indicates whether the marks show up on the bar's groove only, or both the groove and range.
 */
public data class MarkPresentation(val gap: Double = 1.0, val location: MarkLocation = GrooveAndRange)

/**
 * Configures how marks are drawn for circular sliders
 *
 * @property radiusRatio indicates the size of the mark relative to the bar
 * @property location indicates whether the marks show up on the bar's groove only, or both the groove and range.
 */
public data class CircularMarkPresentation(val radiusRatio: Float = 0.1f, val location: MarkLocation = GrooveAndRange)

internal fun getSnapClip(
    marks           : Sequence<Float>,
    orientation     : Orientation,
    grooveRect      : Rectangle,
    grooveRadius    : Double,
    markPresentation: MarkPresentation
): Pair<Path, MarkLocation>? {
    val marks     = marks.toMutableList()
    var offset    = 0.0
    val tickInset = markPresentation.gap / 2

    return when {
        marks.isEmpty() -> null
        else            -> {
            if (marks.last() < 1f) marks += 1f

            marks.toList().map { mark ->
                when (orientation) {
                    Horizontal -> (mark * grooveRect.width).let {
                        Rectangle(
                            x      = grooveRect.x + offset,
                            y      = grooveRect.y,
                            width  = it - offset,
                            height = grooveRect.height
                        ).also { offset = it.right - grooveRect.x }.inset(left = tickInset, right = tickInset).toPath(grooveRadius)
                    }
                    else       -> (mark * grooveRect.height).let {
                        Rectangle(
                            x      = grooveRect.x,
                            y      = grooveRect.y + offset,
                            width  = grooveRect.width,
                            height = it - offset
                        ).also { offset = it.bottom - grooveRect.y }.inset(top = tickInset, bottom = tickInset).toPath(grooveRadius)
                    }
                }
            }.foldRight(Empty.toPath()) { a, b -> a + b } to markPresentation.location
        }
    }
}

internal fun getCircularSnapClip(
    marks           : Sequence<Float>,
    center          : Point,
    outerRadius     : Double,
    innerRadius     : Double,
    startAngle      : Measure<Angle>,
    tickPresentation: CircularMarkPresentation
): Pair<Path, MarkLocation>? {
    val marks = marks.toList()

    return when {
        marks.isEmpty() -> null
        else -> {
            val tickRadius = tickPresentation.radiusRatio * (outerRadius - innerRadius)
            val radiusToHandleCenter = innerRadius + (outerRadius - innerRadius) / 2

            val list = marks.map {
                val angle = startAngle + 360 * degrees * it

                circle(
                    center = center + Point(radiusToHandleCenter * cos(angle), radiusToHandleCenter * sin(angle)),
                    radius = tickRadius,
                    direction = Clockwise,
                )
            }.toMutableList()

            list.add(0, circle(center, innerRadius, Clockwise))
            list.add(circle(center, outerRadius, CounterClockwise))

            list.reduce { a, b -> a + b } to tickPresentation.location
        }
    }
}