package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.geometry.Rectangle

/**
 * Created by Nicholas Eddy on 2/22/19.
 */
class TileLayout(private val spacing: Double = 0.0): Layout() {
    override fun layout(positionable: Positionable) {
        var tileLength = positionable.width - positionable.insets.run { left + right }
        var tileBottom: Double
        var numCols = 1

        do {
            val numRows = ((positionable.children.size / numCols) + 1)
            tileBottom = positionable.insets.top + numRows * tileLength + (numRows - 1) * spacing

            if (tileBottom <= positionable.height - positionable.insets.bottom) {
                break
            }

            ++numCols
            tileLength = (positionable.width - positionable.insets.run { left + right } - (numCols - 1) * spacing) / numCols
        } while (true)

        positionable.children.forEachIndexed { i, child ->
            child.bounds = Rectangle(positionable.insets.left + i % numCols * (tileLength + spacing), positionable.insets.top + i / numCols * (tileLength + spacing), tileLength, tileLength)
        }
    }
}