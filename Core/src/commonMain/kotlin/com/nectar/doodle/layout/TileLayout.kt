package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.geometry.Rectangle

/**
 * Created by Nicholas Eddy on 2/22/19.
 */
class TileLayout(private val spacing: Double = 0.0): Layout() {
    override fun layout(container: PositionableContainer) {
        var tileLength = container.width - container.insets.run { left + right }
        var tileBottom: Double
        var numCols = 1

        do {
            val numRows = ((container.children.size / numCols) + 1)
            tileBottom = container.insets.top + numRows * tileLength + (numRows - 1) * spacing

            if (tileBottom <= container.height - container.insets.bottom) {
                break
            }

            ++numCols
            tileLength = (container.width - container.insets.run { left + right } - (numCols - 1) * spacing) / numCols
        } while (true)

        container.children.forEachIndexed { i, child ->
            child.bounds = Rectangle(container.insets.left + i % numCols * (tileLength + spacing), container.insets.top + i / numCols * (tileLength + spacing), tileLength, tileLength)
        }
    }
}