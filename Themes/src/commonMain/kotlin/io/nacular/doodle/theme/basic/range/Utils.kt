package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.circle
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.theme.basic.range.TickLocation.Groove
import io.nacular.doodle.theme.basic.range.TickLocation.GrooveAndRange
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
 * Determines where ticks are shown for a slider with them shown.
 * @property Groove only draws ticks on the slider's groove
 * @property GrooveAndRange draws ticks on the slider's groove and range
 */
public enum class TickLocation { Groove, GrooveAndRange }

/**
 * Configures how ticks are drawn for sliders
 *
 * @property gap indicates the size of the tick on the bar
 * @property location indicates whether the ticks show up on the bar's groove only, or both the groove and range.
 */
public data class TickPresentation(val gap: Double = 1.0, val location: TickLocation = GrooveAndRange)

/**
 * Configures how ticks are drawn for circular sliders
 *
 * @property radiusRatio indicates the size of the tick relative to the bar
 * @property location indicates whether the ticks show up on the bar's groove only, or both the groove and range.
 */
public data class CircularTickPresentation(val radiusRatio: Float = 0.1f, val location: TickLocation = GrooveAndRange)

internal fun getSnapClip(
    marks           : Sequence<Float>,
    orientation     : Orientation,
    grooveRect      : Rectangle,
    grooveRadius    : Double,
    tickPresentation: TickPresentation
): Pair<Path, TickLocation>? {
    val ticks     = marks.toMutableList()
    var offset    = 0.0
    val tickInset = tickPresentation.gap / 2

    return when {
        ticks.isEmpty() -> null
        else            -> {
            if (ticks.last() < 1f) ticks += 1f

            ticks.toList().map { tick ->
                when (orientation) {
                    Horizontal -> (tick * grooveRect.width).let {
                        Rectangle(
                            x      = grooveRect.x + offset,
                            y      = grooveRect.y,
                            width  = it - offset,
                            height = grooveRect.height
                        ).also { offset = it.right - grooveRect.x }.inset(Insets(left = tickInset, right = tickInset)).toPath(grooveRadius)
                    }
                    else       -> (tick * grooveRect.height).let {
                        Rectangle(
                            x      = grooveRect.x,
                            y      = grooveRect.y + offset,
                            width  = grooveRect.width,
                            height = it - offset
                        ).also { offset = it.bottom - grooveRect.y }.inset(Insets(top = tickInset, bottom = tickInset)).toPath(grooveRadius)
                    }
                }
            }.foldRight(Empty.toPath()) { a, b -> a + b } to tickPresentation.location
        }
    }
}

internal fun getCircularSnapClip(
    marks           : Sequence<Float>,
    center          : Point,
    outerRadius     : Double,
    innerRadius     : Double,
    startAngle      : Measure<Angle>,
    tickPresentation: CircularTickPresentation
): Pair<Path, TickLocation>? {
    val ticks = marks.toList()

    return when {
        ticks.isEmpty() -> null
        else -> {
            val tickRadius = tickPresentation.radiusRatio * (outerRadius - innerRadius)
            val radiusToHandleCenter = innerRadius + (outerRadius - innerRadius) / 2

            val list = ticks.map {
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