package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.times

/**
 * Created by Nicholas Eddy on 3/29/19.
 */

//class LocalPositionableWrapper(private val delegate: PositionableContainer, private val starts: List<Rectangle>, private val progress: Float): PositionableContainer by delegate {
//    override val children: List<Positionable> get() = delegate.children.mapIndexed { index, it ->
//        object: Positionable by it {
//            override var bounds get() = it.bounds; set(end) {
//                val start = starts[index]
//
//                it.bounds = Rectangle(start.position + progress * (end.position - start.position),
//                        Size(start.width + progress * (end.width - start.width), start.height + progress * (end.height - start.height)))
//            }
//
//            override var position get() = it.position; set(value) {
//                bounds = Rectangle(value, size)
//            }
//
//            override var size get() = it.size; set(value) {
//                bounds = Rectangle(position, value)
//            }
//        }
//    }
//}

public class InteractiveLayout(private val start: Layout, private val end: Layout): Layout {
    public var progress: Float = 0f

    override fun layout(container: PositionableContainer) {
        if (progress < 1f) {
            start.layout(container)
        }

        if (progress == 0f) {
            return // done since items are laid out according to start
        }

//        end.layout(
//            when {
//                progress < 1f -> LocalPositionableWrapper(container, container.children.map { it.bounds }, progress)
//                else          -> container
//            }
//        )

        val startBounds = if (progress < 1f) container.children.map { it.bounds } else emptyList()

        end.layout(container)

        if (progress == 1f) {
            return // done since items are laid out according to end
        }

        val endBounds = container.children.map { it.bounds }

        container.children.forEachIndexed { index, view ->
            val start = startBounds[index]
            val end   = endBounds  [index]

            view.bounds = Rectangle(start.position + progress * (end.position - start.position),
                    Size(start.width + progress * (end.width - start.width), start.height + progress * (end.height - start.height)))
        }
    }

    public val inverse: InteractiveLayout get() = InteractiveLayout(end, start)
}