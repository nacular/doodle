package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.drawing.AffineTransform

/**
 * Created by Nicholas Eddy on 3/31/19.
 */
public class TransformLayout(private val transform: (Positionable) -> AffineTransform, private val start: Layout? = null): Layout {
    override fun layout(container: PositionableContainer) {
        start?.layout(container)

        container.children.forEach {
            it.bounds = transform(it)(it.bounds).boundingRectangle
        }
    }
}