package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 3/31/19.
 */
public class TransformLayout(private val transform: (Positionable) -> AffineTransform, private val start: Layout? = null): Layout {
    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
        start?.layout(views, min, current, max)

        views.forEach {
            transform(it)(it.bounds).boundingRectangle.apply {
                it.updateBounds(x, y, size, size)
            }
        }

        return current
    }
}