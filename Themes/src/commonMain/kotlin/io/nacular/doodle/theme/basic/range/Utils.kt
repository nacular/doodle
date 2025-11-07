package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
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
    ticks           : Int,
    orientation     : Orientation,
    grooveRect      : Rectangle,
    grooveRadius    : Double,
    tickPresentation: TickPresentation
): Pair<Path, TickLocation>? = when {
    ticks < 1 -> null
    else      -> {
        val snapSize  = if (orientation == Horizontal) grooveRect.width / (ticks - 1) else grooveRect.height / (ticks - 1)
        val tickInset = tickPresentation.gap / 2

        (0 until ticks).map {
            when (orientation) {
                Horizontal -> Rectangle(
                    x      = grooveRect.x + it * snapSize,
                    y      = grooveRect.y,
                    width  = snapSize,
                    height = grooveRect.height
                ).inset(Insets(left = tickInset, right = tickInset)).toPath(grooveRadius)
                else       -> Rectangle(
                    x      = grooveRect.x,
                    y      = grooveRect.y + it * snapSize,
                    width  = grooveRect.width,
                    height = snapSize
                ).inset(Insets(top = tickInset, bottom = tickInset)).toPath(grooveRadius)
            }
        }.foldRight(Rectangle.Empty.toPath()) { a, b -> a + b } to tickPresentation.location
    }
}

internal fun getCircularSnapClip(
    ticks           : Int,
    center          : Point,
    outerRadius     : Double,
    innerRadius     : Double,
    startAngle      : Measure<Angle>,
    tickPresentation: CircularTickPresentation
): Pair<Path, TickLocation>? = when {
    ticks < 1 -> null
    else      -> {
        val tickAngle            = 360 * degrees / (ticks - 1)
        val tickRadius           = tickPresentation.radiusRatio * (outerRadius - innerRadius)
        val radiusToHandleCenter = innerRadius + (outerRadius - innerRadius) / 2

        val list = (0 until ticks - 1).map {
            val angle = startAngle + tickAngle * it

            circle(
                center    = center + Point(radiusToHandleCenter * cos(angle), radiusToHandleCenter * sin(angle)),
                radius    = tickRadius,
                direction = Clockwise,
            )
        }.toMutableList()

        list.add(0, circle(center, innerRadius, Clockwise       ))
        list.add(   circle(center, outerRadius, CounterClockwise))

        list.reduce { a, b -> a + b } to tickPresentation.location
    }
}