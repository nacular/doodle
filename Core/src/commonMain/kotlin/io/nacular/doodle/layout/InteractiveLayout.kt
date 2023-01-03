package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.lerp
import io.nacular.doodle.utils.lerp

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

    override fun layout(container: PositionableContainer) {
        if (progress < 1f) {
            start.layout(container)
        }

        if (progress == 0f) {
            return // done since items are laid out according to start
        }

        val startBounds = if (progress < 1f) container.children.map { it.bounds } else emptyList()

        end.layout(container)

        if (progress == 1f) {
            return // done since items are laid out according to end
        }

        val endBounds = container.children.map { it.bounds }

        container.children.forEachIndexed { index, view ->
            val start = startBounds[index]
            val end   = endBounds  [index]

            view.bounds = Rectangle(
                lerp(start.position, end.position, progress),
                Size(lerp(start.width, end.width, progress), lerp(start.height, end.height, progress))
            )
        }
    }

    /**
     * An InteractiveLayout that goes from [end] to [start].
     */
    public val inverse: InteractiveLayout by lazy {  InteractiveLayout(end, start) }
}