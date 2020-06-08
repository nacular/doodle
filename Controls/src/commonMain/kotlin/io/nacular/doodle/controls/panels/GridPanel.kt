package io.nacular.doodle.controls.panels

import io.nacular.doodle.controls.panels.SizingPolicy.OverlappingView
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 5/1/20.
 */
interface SizingPolicy {
    data class OverlappingView(val span: Int, val size: Double, val idealSize: Double?)

    operator fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double>
}

class FitContent: SizingPolicy {
    override fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double> = views.mapValues { entry ->
        var size = 0.0

        entry.value.forEach {
            size = max(size, (it.size - spacing * (it.span - 1)) / it.span)
        }

        size
    }
}

class FitPanel: SizingPolicy {
    override fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>) = views.mapValues {
        max(0.0, (panelSize - spacing * (views.size - 1)) / views.size)
    }
}

open class GridPanel: View() {
    private data class Location  (val row: Int, val column: Int)
    private data class Dimensions(var offset: Double = 0.0, var size: Double = 0.0)

    private val locations        = mutableMapOf<View, Set<Location>>()
    private val rowSpans         = mutableMapOf<View, Int>().withDefault { 1 }
    private val columnSpans      = mutableMapOf<View, Int>().withDefault { 1 }
    private var rowDimensions    = mapOf<Int, Dimensions>()
    private var columnDimensions = mapOf<Int, Dimensions>()

    var cellAlignment     : Constraints.() -> Unit = fill //center
    var verticalSpacing                            = 0.0
    var horizontalSpacing                          = 0.0
    var rowSizingPolicy   : SizingPolicy           = FitContent
    var columnSizingPolicy: SizingPolicy           = FitContent

    final override var layout: Layout? = GridLayout()

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
            val rowLanes = mutableMapOf<Int, MutableList<OverlappingView>>()
            val colLanes = mutableMapOf<Int, MutableList<OverlappingView>>()

            // Calculate row and column sizes
            children.forEach { child ->
                locations[child]?.forEach { (row, col) ->
                    val rowSpan = rowSpans.getValue   (child)
                    val colSpan = columnSpans.getValue(child)

                    rowLanes.getOrPut(row) { mutableListOf() }.also { it += OverlappingView(rowSpan, child.size.height, child.idealSize?.height) }
                    colLanes.getOrPut(col) { mutableListOf() }.also { it += OverlappingView(colSpan, child.size.width,  child.idealSize?.width ) }
                }
            }

            rowDimensions    = rowSizingPolicy   (height, verticalSpacing,   rowLanes).mapValues { Dimensions(0.0, it.value) }
            columnDimensions = columnSizingPolicy(width,  horizontalSpacing, colLanes).mapValues { Dimensions(0.0, it.value) }

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

    companion object {
        val FitPanel   = FitPanel  ()
        val FitContent = FitContent()
    }
}