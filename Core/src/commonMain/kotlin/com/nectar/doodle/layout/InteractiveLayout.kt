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
        start.layout(positionable)

        val startBounds = positionable.children.map { it.bounds }

        end.layout(positionable)

        val endBounds = positionable.children.map { it.bounds }

        positionable.children.forEachIndexed { index, view ->
            val start = startBounds[index]
            val end   = endBounds  [index]

            view.bounds = Rectangle(start.position + progress * (end.position - start.position),
                    Size(start.width + progress * (end.width - start.width), start.height + progress * (end.height - start.height)))
        }
    }
}