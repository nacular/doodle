package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.theme.basic.range.TickLocation.GrooveAndRange
import io.nacular.doodle.utils.Orientation

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

internal fun getSnapClip(ticks           : Int,
                         orientation     : Orientation,
                         grooveRect      : Rectangle,
                         tickPresentation: TickPresentation): Pair<Path, TickLocation>? = when {
    ticks < 1 -> null
    else      -> {
        val snapSize  = grooveRect.width / (ticks - 1)
        val tickInset = tickPresentation.gap / 2

        (0 until ticks).map {
            when (orientation) {
                Orientation.Horizontal -> Rectangle(
                    x      = grooveRect.x + it * snapSize,
                    y      = grooveRect.y,
                    width  = snapSize,
                    height = grooveRect.height
                ).inset(Insets(left = tickInset, right = tickInset)).toPath(grooveRect.height / 2)
                else                   -> Rectangle(
                    x      = grooveRect.x,
                    y      = grooveRect.y + it * snapSize,
                    width  = grooveRect.width,
                    height = snapSize
                ).inset(Insets(top = tickInset, bottom = tickInset)).toPath(grooveRect.height / 2)
            }

        }.foldRight(Rectangle.Empty.toPath()) { a, b -> a + b } to tickPresentation.location
    }
}