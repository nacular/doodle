package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.lerp

/**
 * Layout that interpolates linearly between a [start] and [end] based on the value of [progress].
 *
 * @constructor
 * @property start layout that defines how the container's children are placed at progress == 0
 * @property end layout that defines how the container's children are placed at progress == 1
 * @property progress that determines the interpolation point between [start] and [end]
 */
public class InteractiveLayout(private val start: Layout, private val end: Layout): Layout {
    public var progress: Float = 0f

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
        var resultStart = current

        if (progress < 1f) {
            resultStart = start.layout(views, min, current, max, insets)
        }

        if (progress == 0f) {
            return resultStart // done since items are laid out according to start
        }

        val children = views.toList()

        val startBounds = if (progress < 1f) children.map { it.bounds } else emptyList()

        val resultEnd: Size = end.layout(views, min, current, max, insets)

        if (progress == 1f) {
            return resultEnd // done since items are laid out according to end
        }

        val endBounds = children.map { it.bounds }

        views.forEachIndexed { index, view ->
            view.updateBounds(lerp(startBounds[index], endBounds[index], progress))
        }

        return lerp(resultStart, resultEnd, progress)
    }

    /**
     * An InteractiveLayout that goes from [end] to [start].
     */
    public val inverse: InteractiveLayout by lazy { InteractiveLayout(end, start) }
}