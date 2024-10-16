package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 3/29/19.
 */
public class ZoomedLayout(private val index: Int, private val targetBounds: Rectangle? = null, private val layout: Layout? = null): Layout {
    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {

        // Need to first lay out the children
        layout?.layout(views, min, current, max)

        val children = views.toList()

        children.getOrNull(index)?.let { view ->
            // Then we can adjust their bounds using a transform
            val targetBounds  = targetBounds ?: Rectangle(size = current).inset(insets)
            val currentBounds = view.bounds
            val around        = Point(insets.left, insets.top)

            val transform = Identity
                .translate(around)
                .scale    (targetBounds.width / currentBounds.width, targetBounds.height / currentBounds.height)
                .translate(targetBounds.position - currentBounds.position)
                .translate(-around)

            children.forEach {
                val p1   = transform(it.bounds.position                                                  )
                val p2   = transform(Point(it.bounds.x + it.bounds.width, it.bounds.y + it.bounds.height))
                val size = Size(p2.x - p1.x, p2.y - p1.y)

                it.updateBounds(p1.x, p1.y, size, size)
            }
        }

        return current
    }
}