package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.times

/**
 * Created by Nicholas Eddy on 3/29/19.
 */
class InteractiveLayout(private val start: Layout, private val end: Layout): Layout() {
    var progress = 0f

    override fun layout(positionable: Positionable) {
        if (progress < 1f) {
            start.layout(positionable)
        }

        if (progress == 0f) {
            return // done since items are laid out according to start
        }

        val startBounds = if (progress != 1f) positionable.children.map { it.bounds } else emptyList()

        end.layout(positionable)

        if (progress == 1f) {
            return // done since items are laid out according to end
        }

        val endBounds = positionable.children.map { it.bounds }

        positionable.children.forEachIndexed { index, view ->
            val start = startBounds[index]
            val end   = endBounds  [index]

            view.bounds = Rectangle(start.position + progress * (end.position - start.position),
                    Size(start.width + progress * (end.width - start.width), start.height + progress * (end.height - start.height)))
        }
    }

    val inverse get() = InteractiveLayout(end, start)
}