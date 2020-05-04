package com.nectar.doodle.controls.panels

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.layout.fill
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 5/1/20.
 */
class GridPanel: View() {
    private data class Location(val row: Int, val column: Int)
    private data class Dimensions(var offset: Double = 0.0, var size: Double = 0.0)

    private val locations        = mutableMapOf<View, Set<Location>>()
    private val rowDimensions    = mutableMapOf<Int, Dimensions>()
    private val rowSpans         = mutableMapOf<View, Int>().withDefault { 1 }
    private val columnDimensions = mutableMapOf<Int, Dimensions>()
    private val columnSpans      = mutableMapOf<View, Int>().withDefault { 1 }

    var cellAlignment: (Constraints.() -> Unit) = fill //center
    var verticalSpacing   = 0.0
    var horizontalSpacing = 0.0

    init {
        layout = GridLayout()
    }

    fun add(child: View, row: Int = 0, column: Int = 0, rowSpan: Int = 1, columnSpan: Int = 1) {
        locations[child] = mutableSetOf<Location>().apply {
            repeat(rowSpan) { r ->
                repeat(columnSpan) { c ->
                    add(Location(row = max(0, row + r), column = max(0, column + c)))
                }
            }
        }

        if (rowSpan    > 1) rowSpans   [child] = rowSpan
        if (columnSpan > 1) columnSpans[child] = columnSpan

        children += child
    }

    private inner class GridLayout: Layout {
        private var idealWidth  = null as Double?
        private var idealHeight = null as Double?

        override fun idealSize(container: PositionableContainer, default: Size?) = idealWidth?.let { w ->
            idealHeight?.let { h ->
                Size(w, h)
            }
        }

        override fun layout(container: PositionableContainer) {
            // Calculate row and column sizes
            children.forEach { child ->
                locations[child]?.forEach { (row, col) ->
                    val rowSpan   = rowSpans.getValue   (child)
                    val colSpan   = columnSpans.getValue(child)
                    val childSize = child.idealSize ?: child.size

                    rowDimensions.getOrPut   (row) { Dimensions() }.apply { size = max(size, (childSize.height - verticalSpacing   * (rowSpan - 1)) / rowSpan) }
                    columnDimensions.getOrPut(col) { Dimensions() }.apply { size = max(size, (childSize.width  - horizontalSpacing * (colSpan - 1)) / colSpan) }
                }
            }

            var offset = 0.0
            rowDimensions.entries.sortedBy { it.key }.forEach {
                it.value.offset = offset
                offset += it.value.size + verticalSpacing
            }

            idealHeight = offset - if (rowDimensions.size > 1) verticalSpacing else 0.0

            offset = 0.0
            columnDimensions.entries.sortedBy { it.key }.forEach {
                it.value.offset = offset
                offset += it.value.size + horizontalSpacing
            }

            idealWidth = offset - if (columnDimensions.size > 1) horizontalSpacing else 0.0

            children.forEach { child ->
                var x       = null as Double?
                var y       = null as Double?
                val widths  = mutableSetOf<Dimensions>()
                val heights = mutableSetOf<Dimensions>()

                locations[child]?.forEach { (row, col) ->
                    val rowDim = rowDimensions.getValue   (row)
                    val colDim = columnDimensions.getValue(col)

//                    println("[$row,$col] -> $rowDim, $colDim")

                    x = min(x ?: colDim.offset, colDim.offset)
                    y = min(y ?: rowDim.offset, rowDim.offset)
                    widths.add (colDim)
                    heights.add(rowDim)
                }

                constrain(child,
                        within = Rectangle(
                                x ?: 0.0,
                                y ?: 0.0,
                                widths.map  { it.size }.sum() + horizontalSpacing * (widths.size  - 1),
                                heights.map { it.size }.sum() + verticalSpacing   * (heights.size - 1)),
                        block = cellAlignment)
            }
        }
    }
}