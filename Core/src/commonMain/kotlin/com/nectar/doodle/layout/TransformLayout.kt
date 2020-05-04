package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.drawing.AffineTransform

/**
 * Created by Nicholas Eddy on 3/31/19.
 */
class TransformLayout(private val transform: (Positionable) -> AffineTransform, private val start: Layout? = null): Layout {
    override fun layout(container: PositionableContainer) {
        start?.layout(container)

        container.children.forEach {
            it.bounds = transform(it)(it.bounds).boundingRectangle
        }
    }
}