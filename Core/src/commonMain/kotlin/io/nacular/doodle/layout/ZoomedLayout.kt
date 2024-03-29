package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle

/**
 * Created by Nicholas Eddy on 3/29/19.
 */
public class ZoomedLayout(private val index: Int, private val targetBounds: Rectangle? = null, private val layout: Layout? = null): Layout {
    override fun layout(container: PositionableContainer) {

        // Need to first lay out the children
        layout?.layout(container)

        container.children.getOrNull(index)?.let { view ->
            // Then we can adjust their bounds using a transform
            val targetBounds  = targetBounds ?: Rectangle(size = container.size).inset(container.insets)
            val currentBounds = view.bounds
            val around        = Point(container.insets.left, container.insets.top)

            val transform = Identity.
                    translate(around).
                    scale    (targetBounds.width / currentBounds.width, targetBounds.height / currentBounds.height).
                    translate(targetBounds.position - currentBounds.position).
                    translate(-around)

            container.children.forEach {
                val p1 = transform(it.position                             )
                val p2 = transform(Point(it.x + it.width, it.y + it.height))

                it.bounds = Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y)
            }
        }
    }
}