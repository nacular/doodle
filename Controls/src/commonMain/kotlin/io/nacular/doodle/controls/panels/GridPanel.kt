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
public interface SizingPolicy {
    public class OverlappingView(public val span: Int, public val size: Double, public val idealSize: Double?)

    public operator fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double>
}

public typealias SpacingPolicy = (panelSize: Double) -> Double

@Deprecated(message = "Use constant instead", replaceWith = ReplaceWith("GridPanel.Companion.FitContent"))
public class FitContent: SizingPolicy {
    override fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double> = views.mapValues { entry ->
        var size = 0.0

        entry.value.forEach {
            size = max(size, (it.size - spacing * (it.span - 1)) / it.span)
        }

        size
    }
}

@Deprecated(message = "Use constant instead", replaceWith = ReplaceWith("GridPanel.Companion.FitPanel"))
public class FitPanel: SizingPolicy {
    override fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double> = views.mapValues {
        max(0.0, (panelSize - spacing * (views.size - 1)) / views.size)
    }
}

public open class GridPanel: View() {
    private class Location(val row: Int, val column: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Location) return false

            if (row != other.row) return false
            if (column != other.column) return false

            return true
        }

        override fun hashCode(): Int {
            var result = row
            result = 31 * result + column
            return result
        }
    }

    private class Dimensions(var offset: Double = 0.0, var size: Double = 0.0) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Dimensions) return false

            if (offset != other.offset) return false
            if (size != other.size) return false

            return true
        }

        override fun hashCode(): Int {
            var result = offset.hashCode()
            result = 31 * result + size.hashCode()
            return result
        }
    }

    private val locations        = mutableMapOf<View, Set<Location>>()
    private val rowSpans         = mutableMapOf<View, Int>().withDefault { 1 }
    private val columnSpans      = mutableMapOf<View, Int>().withDefault { 1 }
    private var rowDimensions    = mapOf<Int, Dimensions>()
    private var columnDimensions = mapOf<Int, Dimensions>()

    public var cellAlignment     : Constraints.() -> Unit = fill //center
    public var verticalSpacing   : SpacingPolicy          = { 0.0 }
    public var horizontalSpacing : SpacingPolicy          = { 0.0 }
    public var rowSizingPolicy   : SizingPolicy           = FitContent
    public var columnSizingPolicy: SizingPolicy           = FitContent

    final override var layout: Layout? = GridLayout()

    public fun add(child: View, row: Int = 0, column: Int = 0, rowSpan: Int = 1, columnSpan: Int = 1) {
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

    public fun remove(child: View) {
        locations   -= child
        rowSpans    -= child
        columnSpans -= child
        children    -= child
    }

    public fun clear() {
        locations.clear  ()
        rowSpans.clear   ()
        columnSpans.clear()
        children.clear   ()
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
                locations[child]?.forEach {
                    val rowSpan = rowSpans.getValue   (child)
                    val colSpan = columnSpans.getValue(child)

                    rowLanes.getOrPut(it.row   ) { mutableListOf() }.also { it += OverlappingView(rowSpan, child.size.height, child.idealSize?.height) }
                    colLanes.getOrPut(it.column) { mutableListOf() }.also { it += OverlappingView(colSpan, child.size.width,  child.idealSize?.width ) }
                }
            }

            val verticalSpacing   = verticalSpacing  (height)
            val horizontalSpacing = horizontalSpacing(width )

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

                locations[child]?.forEach {
                    val rowDim = rowDimensions.getValue   (it.row   )
                    val colDim = columnDimensions.getValue(it.column)

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

            container.idealSize = Size(idealWidth!!, idealHeight!!)
        }
    }

    public companion object {
        public val FitPanel  : SizingPolicy = FitPanel  ()
        public val FitContent: SizingPolicy = FitContent()
    }
}