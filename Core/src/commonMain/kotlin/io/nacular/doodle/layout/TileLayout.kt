package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 2/22/19.
 */
public class TileLayout(private val spacing: Double = 0.0): Layout {
    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size): Size {
        var tileLength = current.width
        var tileBottom: Double
        var numCols = 1

        val children = views.toList()

        do {
            val numRows = (children.size / numCols) + 1
            tileBottom  = numRows * tileLength + (numRows - 1) * spacing

            if (tileBottom <= current.height) {
                break
            }

            ++numCols
            tileLength = (current.width - (numCols - 1) * spacing) / numCols
        } while (true)

        val size = Size(tileLength)

        children.forEachIndexed { i, child ->
            child.updateBounds(i % numCols * (tileLength + spacing), i / numCols * (tileLength + spacing), size, size)
        }

        return current
    }
}