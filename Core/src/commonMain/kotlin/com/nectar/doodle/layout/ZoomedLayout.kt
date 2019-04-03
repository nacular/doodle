package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle

/**
 * Created by Nicholas Eddy on 3/29/19.
 */
class ZoomedLayout(private val index: Int, private val targetBounds: Rectangle? = null, private val layout: Layout? = null): Layout() {
    override fun layout(positionable: Positionable) {
        // Need to first layout the children
        layout?.layout(positionable)

        positionable.children.getOrNull(index)?.let { view ->
            // Then we can adjust their bounds using a transform
            val targetBounds  = targetBounds ?: Rectangle(size = positionable.size).inset(positionable.insets)
            val currentBounds = view.bounds
            val around        = Point(positionable.insets.left, positionable.insets.top)

            val transform = AffineTransform.Identity.
                    translate(around).
                    scale    (targetBounds.width / currentBounds.width, targetBounds.height / currentBounds.height).
                    translate(targetBounds.position - currentBounds.position).
                    translate(-around)

            positionable.children.forEach {
                it.bounds = transform(it.bounds).boundingRectangle
            }
        }
    }
}